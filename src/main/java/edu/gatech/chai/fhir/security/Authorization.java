/*******************************************************************************
 * Copyright (c) 2019 Georgia Tech Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package edu.gatech.chai.fhir.security;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;

/**
 * @author MC142
 *
 */
public class Authorization {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Authorization.class);

	private String url;
	private String authBasic;
	private String token;
	private String token_type;
	private String patient;
	private int myTimeSkewAllowance = 300;
	private boolean active = false;
	private boolean expired = true;
	private boolean is_admin = false;
	private Set<String> scopeSet;

	public Authorization(String url) {
		this.url = url;
		setAuthBasic("client:secret");
	}

	public Authorization(String url, String authBasic) {
		this.url = url;
		this.authBasic = authBasic;
	}

	public String getAuthBasic() {
		return authBasic;
	}

	public void setAuthBasic(String authBasic) {
		this.authBasic = authBasic;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	private HttpHeaders createHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		byte[] encodedAuth = Base64.encodeBase64(authBasic.getBytes(Charset.forName("US-ASCII")));
		String authHeader = "Basic " + new String(encodedAuth);
		httpHeaders.set("Authorization", authHeader);
		httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		return httpHeaders;
//		return new HttpHeaders() {
//			{
//				String auth = clientId+":"+clientSecret;
//				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
//				String authHeader = "Basic " + new String(encodedAuth);
//				set("Authorization", authHeader);
//				setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//			}
//		};
	}

	public String introspectToken(HttpServletRequest request) {
		token_type = null;

		String authString = request.getHeader("Authorization");
		if (authString == null)
			return "No Authorization Header";

		logger.debug("asBearerAuth Authorization header:" + authString);

		if (authString.regionMatches(0, "Bearer", 0, 6) == false)
			return "Invalid Bearer Token: " + authString;

		String accessToken = authString.substring(7);

		logger.debug("Access Token for Introspect:" + accessToken);
		if (introspectToken(accessToken) == false) {
			return "Invalid or Expired Access Token";
		}

		return "";
	}

	public boolean introspectToken(String token) {
		// Sanity Check.
		if (token == null || token.isEmpty()) {
			return false;
		}

		// Save the token for a future use.
		this.token = token;

		// Introspect the token
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> reqAuth = new HttpEntity<String>(createHeaders());
		ResponseEntity<String> response;

		String introspectTokenUrl = url + "?token=" + this.token;
		response = restTemplate.exchange(introspectTokenUrl, HttpMethod.POST, reqAuth, String.class);
		HttpStatusCode statusCode = response.getStatusCode();
		if (statusCode.is2xxSuccessful() == false) {
			logger.debug("Introspect (token:" + token + ") response with statusCode:" + statusCode.toString());
			return false;
		}

		System.out.println("IntrospectToken: " + response.getBody() + ", token: " + token);

		// First check the token status. Turn the body into JSON.
		JSONObject jsonObject = new JSONObject(response.getBody());
		if (jsonObject.getBoolean("active") != true) {
			// This is not active token.
			active = false;
			logger.debug("Introspect response with non-Active token, meaning that the token might be missing or expired");
			return false;
		}
		active = true;

		// Get the expiration time.
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		if (jsonObject.has("exp")) {
			Long exp_ts = jsonObject.getInt("exp") * 1000L;
			Long now = (new Date()).getTime();

			if (exp_ts <= (now - myTimeSkewAllowance)) {
				expired = true;
				logger.debug("Introspect response with expired token");
				return false;
			}

			// Date expDate;
//			try {
//				expDate = new java.util.Date((long) exp_ts * 1000);
////			expDate = df.parse(exp_str);
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				expired = true;
//				return false;
//			}
//
//			Date minAllowableExpirationTime = new Date(System.currentTimeMillis() - (myTimeSkewAllowance * 1000L));
//			if (expDate != null && !expDate.before(minAllowableExpirationTime)) {
//				// expired.
//				expired = true;
//				return false;
//			}
		}
		// Store the received information such as scope, user_id, client_id, etc...
		String userId = null;
		if (jsonObject.has("sub"))
			userId = jsonObject.getString("sub"); // current found no need to use this.
		if (jsonObject.has("token_type"))
			token_type = jsonObject.getString("token_type");
		if (jsonObject.has("patient") && !jsonObject.isNull("patient"))
			patient = jsonObject.getString("patient");

		String[] scopeValues = jsonObject.getString("scope").trim().replaceAll("\\+", " ").split(" ");
		scopeSet = new HashSet<String>(Arrays.asList(scopeValues));
		if (scopeSet.isEmpty()) {
			logger.debug("ScopeSet has empty scopes");
			return false;
		}

		if (scopeSet.contains("user/*.*") || scopeSet.contains("system/*.*")) {
			is_admin = true;
		}

		return true;
	}

	public boolean checkBearer() {
		if (token_type != null && token_type.equalsIgnoreCase("Bearer")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean assertScope(String myScope) {
		for (String scope : scopeSet) {
			if (scope.equalsIgnoreCase(myScope))
				return true;
		}

		return false;
	}

	public boolean allowRequest(RequestDetails theRequestDetails) {
		if (checkBearer() == false) {
			return false;
		}

		if (is_admin) {
			return true;
		}

		// TODO: Check the request detail and compare with scope. If out of scope, then
		// return false.
		// We need to have user or patient level permission checking. For now, user/ and
		// patient/ scope has
		// all patients permission. We need to revisit this.
		//
		String resourceName = theRequestDetails.getResourceName();
		if (resourceName == null) {
			Map<String, String[]> params = theRequestDetails.getParameters();
			String[] page_ = params.get("_getpages");
			if (page_.length > 0) {
				String page_id = page_[0];
				if (page_id != null && !page_id.isEmpty()) {
					// This is page loading. Then, it means the original request passed.
					// If this is wrong page_id, then the server will not able to locate that.
					System.out.println("[THIS SHOULD NOT HAPPEND] Page loading: " + page_id
							+ ". We are assuming this has already authorized.");
					logger.info("[THIS SHOULD NOT HAPPEND] Request (" + theRequestDetails.getCompleteUrl()
							+ ") is page request with id=" + page_id + " We are assuming this has already authorized.");

					return true;
				}
			}
		}

		RestOperationTypeEnum resourceOperationType = theRequestDetails.getRestOperationType();
		for (String scope : scopeSet) {
			// If the scope is not form of <patient or user>/<resource>.<access>, (eg)
			// patient/*.read,
			// then, we skip to next scope as this only evaluates the resource level check.
			String patternString = "(user|patient|system)\\/[a-zA-Z*]+.(read|write|\\*)";
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(scope);
			if (matcher.matches() == false)
				continue;

			String[] scopeDetail = scope.split("/");
			logger.debug("Requesting resource: " + resourceName + ". checking scope:" + scope + " scopeDetail[0]="
					+ scopeDetail[0]);
			if ("patient".equals(scopeDetail[0])) {
				if (patient == null || patient.isEmpty())
					continue;

				boolean found = false;
				if ("Patient".equals(resourceName)) {
					if (theRequestDetails.getId() != null && theRequestDetails.getId().getIdPart() != null
							&& !theRequestDetails.getId().getIdPart().isEmpty()) {
						String patientIdInRequest = theRequestDetails.getId().getIdPart();
						logger.debug("retrieving Patient by client. This scoped is bound to patient_id:" + patient
								+ " and received patient:" + patientIdInRequest);
						if (!patient.equals(theRequestDetails.getId().getIdPart())) {
							continue;
						} else {
							found = true;
						}
					} else {
						Map<String, String[]> reqParam = theRequestDetails.getParameters();
						String[] patientIds = reqParam.get("_id");
						if (patientIds == null || patientIds.length == 0) {
							patientIds = reqParam.get("id");
						}

						if (patientIds != null && patientIds.length > 0) {
							for (String patientId : patientIds) {
								logger.debug("retrieving Patient by client. This scoped is bound to patient_id:"
										+ patient + " and received patient (id= or _id=):" + patientId);
								if (patient.equals(patientId)) {
									found = true;
									break;
								}
							}
						}
					}
				} else {
					// Trying to get a resource other than Patient. Check if we have
					// patient or subject parameters. If not, throw exception.
					Map<String, String[]> reqParam = theRequestDetails.getParameters();
					String[] patientIds = reqParam.get("patient");
					if (patientIds == null || patientIds.length == 0) {
						patientIds = reqParam.get("subject");
						if (patientIds != null && patientIds.length > 0) {
							found = false;
							for (String patientId : patientIds) {
								logger.debug("Checking subject=Patient/<id>:" + patientId
										+ " with <id> in Introspect response:" + patient);
								if (patientId.startsWith("Patient/")) {
									String id = patientId.substring(8);
									if (id.equals(patient)) {
										found = true;
										break;
									}
								} else {
									logger.error(
											"subject= parameter is required to have subject related Resource. Resource/ is not found. Request URL:"
													+ theRequestDetails.getCompleteUrl());
								}
							}
						} else {
							patientIds = reqParam.get("subject:Patient");
							if (patientIds != null && patientIds.length > 0) {
								found = false;
								for (String patientId : patientIds) {
									logger.debug("Checking subjec:Patientt=<id>:" + patientId
											+ " with <id> in Introspect response:" + patient);
									if (patientId.equals(patient)) {
										found = true;
										break;
									}
								}
							}
						}
					} else {
						// patient id.
						found = false;
						for (String patientId : patientIds) {
							logger.debug("Checking patient=<id>:" + patientId + " with <id> in Introspect response:"
									+ patient);
							if (patient.equals(patientId)) {
								found = true;
								break;
							}
						}
					}
				}

				if (!found) {
					logger.debug("Couldn't find patient/ scope matches with patient id=" + patient + ". Request URL:"
							+ theRequestDetails.getCompleteUrl());
					continue;
				} else {
					logger.debug("Found! patient/ scope matches with patient id=" + patient);
				}
			}

			if (resourceOperationType == RestOperationTypeEnum.READ
					|| resourceOperationType == RestOperationTypeEnum.VREAD
					|| resourceOperationType == RestOperationTypeEnum.SEARCH_TYPE) {
				if ((scopeDetail[1].equalsIgnoreCase("*.read") || scopeDetail[1].equalsIgnoreCase("*.*"))) {
					return true;
				} else {
					String[] scopeResource = scopeDetail[1].split("\\.");
					if (scopeResource[0].equalsIgnoreCase(resourceName)
							&& (scopeResource[1].equalsIgnoreCase("read") || scopeResource[1].equalsIgnoreCase("*"))) {
						return true;
					}
				}
			} else {
				// This is CREATE, UPDATE, DELETE... write permission is required.
				if ((scopeDetail[1].equalsIgnoreCase("*.write") || scopeDetail[1].equalsIgnoreCase("*.*"))) {
					return true;
				} else {
					String[] scopeResource = scopeDetail[1].split("\\.");
					if (scopeResource[0].equalsIgnoreCase(resourceName)
							&& (scopeResource[1].equalsIgnoreCase("write") || scopeResource[1].equalsIgnoreCase("*"))) {
						return true;
					}
				}
			}
		}

		System.out.println(resourceName + " " + resourceOperationType.name() + " request failed to get Authorization.");
		logger.error("Request (" + theRequestDetails.getCompleteUrl() + ") is not authorized:" + resourceName + " "
				+ resourceOperationType.name() + " request failed to get Authorization.");
		return false;
	}

	// Belows are for out-of-band authorization to support Smart on FHIR internal
	// communications.
	// This is not Smart on FHIR standard. This is to support Smart on FHIR's
	// authorization server.
	public boolean asBasicAuth(HttpServletRequest request) {
		String authString = request.getHeader("Authorization");
		if (authString == null)
			return false;

		logger.debug("asBasicAuth Authorization header:" + authString);
//		String[] credential = OAuthUtils.decodeClientAuthenticationHeader(authString);

		if (authString.regionMatches(0, "Basic", 0, 5) == false)
			return false; // Not a basic Auth

		String credentialString = StringUtils.newStringUtf8(Base64.decodeBase64(authString.substring(6)));
		if (credentialString == null)
			return false;

//		String[] credential = credentialString.trim().split(":");
//
//		if (credential.length != 2)
//			return false;
//
//		userId = credential[0];
//		password = credential[1];
//
//		logger.debug("asBasicAuth:" + userId + ":" + password);
//		if (userId.equalsIgnoreCase(clientId) && password.equalsIgnoreCase(clientSecret))
		if (authBasic.equals(credentialString))
			return true;
		else
			return false;
	}

	public boolean asBearerAuth(HttpServletRequest request) {
		String authString = request.getHeader("Authorization");
		if (authString == null)
			return false;

		logger.debug("asBearerAuth Authorization header:" + authString);

		if (authString.regionMatches(0, "Bearer", 0, 6) == false)
			return false;

		String accessToken = authString.substring(7);

		return introspectToken(accessToken);
	}
}
