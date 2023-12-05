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
package edu.gatech.chai.fhir.smart.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
//import javax.servlet.annotation.WebServlet;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import edu.gatech.chai.fhir.smart.Jwt.JwtUtil;
import edu.gatech.chai.fhir.smart.dao.JwkSetImpl;
import edu.gatech.chai.fhir.smart.dao.SmartOnFhirAppImpl;
import edu.gatech.chai.fhir.smart.dao.SmartOnFhirSessionImpl;
import edu.gatech.chai.fhir.smart.model.JwkSetEntry;
import edu.gatech.chai.fhir.smart.model.SmartOnFhirAppEntry;
import edu.gatech.chai.fhir.smart.model.SmartOnFhirAppListContainer;
import edu.gatech.chai.fhir.smart.model.SmartOnFhirSessionEntry;
import edu.gatech.chai.fhir.smart.servlet.response.IntrospectResponse;
import edu.gatech.chai.fhir.smart.servlet.response.OAuth2Error;
import edu.gatech.chai.fhir.smart.servlet.response.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;

/**
 * auth/ implementation for SMART on FHIR support for authentication
 * 
 */

@Controller
@SessionAttributes("oauth2attr")
public class SmartAuthServicesController {
	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(SmartAuthServicesController.class);

	private String authBasic;
	private String jwtSecret;
	private String smartStyleUrl;
	private boolean simEhr;
	private int accessTokenTimeoutMinutes;

	private String baseUrl = "";

	@Autowired
	protected SmartOnFhirAppImpl smartOnFhirApp;

	@Autowired
	protected JwkSetImpl jwkSet;

	@Autowired
	protected SmartOnFhirSessionImpl smartOnFhirSession;

	public static final int timeout_min = 5;

	public SmartAuthServicesController() {
		super();

		String serverBaseUrl = System.getenv("SERVERBASE_URL");
		if (serverBaseUrl != null && !serverBaseUrl.isEmpty()) {
			if (serverBaseUrl.endsWith("/")) {
				serverBaseUrl = serverBaseUrl.substring(0, serverBaseUrl.length() - 2);
			}

			int lastSlashIndex = serverBaseUrl.lastIndexOf("/");
			if (lastSlashIndex > 0) {
				baseUrl = serverBaseUrl.substring(0, lastSlashIndex);
			}
		}

		authBasic = System.getenv("AUTH_BASIC");

		if (authBasic == null)
			authBasic = "client:secret";

		jwtSecret = System.getenv("JWT_SECRET");
		if (jwtSecret == null) {
			jwtSecret = "thisismysecret";
		}

		smartStyleUrl = System.getenv("SMART_STYLE_URL");
		if (smartStyleUrl == null) {
			smartStyleUrl = "http://localhost/smart-style.json";
		}

		simEhr = false;

		if (System.getenv("ACCESS_TOKEN_TIMEOUT_MIN") != null) {
			accessTokenTimeoutMinutes = Integer.valueOf(System.getenv("ACCESS_TOKEN_TIMEOUT_MIN"));
		} else {
			accessTokenTimeoutMinutes = SmartAuthServicesController.timeout_min;
		}

	}

//	@ModelAttribute("oauth2attr")
//	public JSONObject oauth2attr() {
//		return new JSONObject();
//	}

	private JSONObject decodeJWT(String jwtToken) {
		String[] jwtSplitted = jwtToken.split("\\.");

		String jwtBodyBase64 = jwtSplitted[1];
		String jwtBody = new String(Base64.decodeBase64(jwtBodyBase64));

		return new JSONObject(jwtBody);
	}

	private String getPatientIdFromJWT(String code) {
		JSONObject jwtBodyJson = decodeJWT(code);
		if (jwtBodyJson.has("context")) {
			JSONObject context = jwtBodyJson.getJSONObject("context");
			if (context.has("patient")) {
				return context.getString("patient");
			}
		}

		return null;
	}

	private String getPatientFromLaunchContext(String launchContext) {
		if (launchContext == null || launchContext.isEmpty()) {
			return null;
		}

		String launchCode = new String(Base64.decodeBase64(launchContext));
		logger.debug("Launch Code:" + launchCode);

		// decode the code.
		JSONObject codeJson = new JSONObject(launchCode);
		JSONObject decodedCode = SmartLauncherCodec.decode(codeJson);
		if (decodedCode.has("patient")) {
			return decodedCode.getString("patient");
		}

		return null;
	}

	private String generateJWT(String launchContext, String scope, SmartOnFhirAppEntry smartApp) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		Date expiration = new Date(nowMillis + 300000); // 5m later

		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtSecret);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		JSONObject payload = new JSONObject();
		JSONObject context = new JSONObject();
		context.put("need_patient_banner", !simEhr);
		context.put("smart_style_url", smartStyleUrl);

		String patientId = getPatientFromLaunchContext(launchContext);
		if (patientId != null && !patientId.isEmpty())
			context.put("patient", patientId);

		payload.put("context", context);
		payload.put("client_id", smartApp.getAppId());
		payload.put("scope", scope);
		payload.put("iat", now.getTime() / 1000);
		payload.put("exp", expiration.getTime() / 1000);

		JwtBuilder jwtBuilder = Jwts.builder().setHeaderParam(Header.TYPE, Header.JWT_TYPE)
				.setPayload(payload.toString()).signWith(signingKey, signatureAlgorithm);
		return jwtBuilder.compact();
	}

	private String encodeValue(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
	}

	@GetMapping(value = "/authorize")
	public ModelAndView authorize(@RequestParam(name = "launch", required = false) String launchContext,
			@RequestParam(name = "response_type", required = false) String responseType,
			@RequestParam(name = "client_id", required = false) String clientId,
			@RequestParam(name = "redirect_uri", required = true) String redirectUri,
			@RequestParam(name = "scope", required = false) String scope,
			@RequestParam(name = "aud", required = false) String aud,
			@RequestParam(name = "state", required = false) String state, ModelMap model) {

		String errorDesc;
		String error;

		// Alway pass this information so that JSP can route to correct endpoint
		model.addAttribute("base_url", baseUrl);

		SmartOnFhirAppEntry smartApp = smartOnFhirApp.getSmartOnFhirApp(clientId);
		if (smartApp == null) {
			// Invalid client-id. We shour send with bad request.
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid client_id");
		}

		if (!redirectUri.equals(smartApp.getRedirectUri())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid redirect_url");
		}

		if (responseType == null || !"code".equals(responseType)) {
			try {
				error = "unsupported_response_type";
				errorDesc = encodeValue(
						"The authorization server does not support obtaining an authorization code using this method");
				model.addAttribute("error", error);
				model.addAttribute("error_description", errorDesc);
				model.addAttribute("state", state);
				return new ModelAndView("redirect:" + redirectUri, model);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", e);
			}
		}

		// We should use the client Id and check the scope to decide if
		// we want to authenticate this app request.
		// But, for now, we authenticate this.
		// TODO: connect to Authenticate server to authenticate the app.
		// One idea is to use Google or Facebook.
		//
		// For now, we expect the public app. client_id is required.
		// http://www.hl7.org/fhir/smart-app-launch/#step-3-app-exchanges-authorization-code-for-access-token

		// scope check. We need to make sure if this app has these scopes registered.
		// But, for this simulation, we allow everything.
		// If scope has launch, it should have launchContext as it's launched from EHR
		// If scope has launch/patient, then we need to run patient browser
		// If scope has launch/encounter, then we need to run encounter browser
		if (scope == null || scope.isEmpty()) {
			try {
				error = "invalid_scope";
				errorDesc = encodeValue("The requested scope is invalid, unknown, or malformed");
				model.addAttribute("error", error);
				model.addAttribute("error_description", errorDesc);
				model.addAttribute("state", state);
				return new ModelAndView("redirect:" + redirectUri, model);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", e);
			}
		}

		if (scope.contains("launch") && launchContext == null) {
			try {
				error = "invalid_request";
				errorDesc = encodeValue(
						"The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than once, or is otherwise malformed");
				model.addAttribute("error", error);
				model.addAttribute("error_description", errorDesc);
				model.addAttribute("state", state);
				return new ModelAndView("redirect:" + redirectUri, model);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", e);
			}
		}

		// Check the scope.
		String[] scopeEntries = scope.split(" ");
		String myScope = smartApp.getScope();
		for (String scopeEntry : scopeEntries) {
			if (!myScope.contains(scopeEntry)) {
				// Out of scope
				try {
					logger.info("scope, " + scopeEntry + ", is not valid");
					error = "invalid_scope";
					errorDesc = encodeValue("The requested scope is invalid, unknown, or malformed");
					model.addAttribute("error", error);
					model.addAttribute("error_description", errorDesc);
					model.addAttribute("state", state);
					return new ModelAndView("redirect:" + redirectUri, model);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", e);
				}
			}
		}

		// The launchContext, if exists, contains a context to resolve this to
		// patient, encounter, provider, etc. We used the encoding that smart on fhir
		// launcher is using.
		if (launchContext != null && !launchContext.isEmpty()) {
			String launchCode = new String(Base64.decodeBase64(launchContext));
			logger.debug("Launch Code:" + launchCode);

			// decode the code.
			JSONObject codeJson = new JSONObject(launchCode);
			JSONObject decodedCode = SmartLauncherCodec.decode(codeJson);
			if (decodedCode.has("launch_ehr") && "1".equals(decodedCode.getString("launch_ehr"))) {
				// We are launching in EHR mode.
				// Do something here if you need to do anything
			}

			if (decodedCode.has("sim_ehr") && "1".equals(decodedCode.getString("sim_ehr"))) {
				simEhr = true;

				// We are simulating EHR.
				if (decodedCode.has("auth_error")) {
					// Auth error simulation is requested.
					// Return error as requested.
					error = decodedCode.getString("auth_error");
					errorDesc = SmartLauncherCodec.getSimErrorDesc(error);
					model.addAttribute("error", error);
					model.addAttribute("error_description", errorDesc);
					model.addAttribute("state", state);
					return new ModelAndView("redirect:" + redirectUri, model);
				}
			}
//
//			patientId = decodedCode.getString("patient");
		} else {
			// TODO: Handle patient choose
		}

		// redirect to authentication page.
		model.addAttribute("auth_request_validated", true);
		model.addAttribute("launch", launchContext);
		model.addAttribute("response_type", responseType);
		model.addAttribute("client_id", clientId);
		model.addAttribute("redirect_uri", redirectUri);
		model.addAttribute("scope", scope);
		model.addAttribute("aud", aud);
		model.addAttribute("state", state);

//		SmartAuthRequest authRequest = new SmartAuthRequest();
//		authRequest.setAppEntry(smartApp);
//		authRequest.setLaunchContext(launchContext);
//		authRequest.setState(state);
//		authRequest.setScope(scope);
//		authRequest.setServerBaseUrl(baseUrl);		
//		return new ModelAndView("authenticate", "request", authRequest);

		return new ModelAndView("authenticate", model);
	}

	private static final SecureRandom secureRandom = new SecureRandom(); // threadsafe
	private static final java.util.Base64.Encoder base64Encoder = java.util.Base64.getUrlEncoder(); // threadsafe

	public static String generateNewToken() {
		byte[] randomBytes = new byte[24];
		secureRandom.nextBytes(randomBytes);
		return base64Encoder.encodeToString(randomBytes);
	}

	private String getAToken() {
		String token = null;
		
		boolean exists = true;
		while (exists) {
			token = SmartAuthServicesController.generateNewToken();
			if (smartOnFhirSession.getSmartOnFhirAppByToken(token) == null)
				exists = false;
		}
		
		return token;
	}
	
	@RequestMapping(value = "/token", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
//	public String token(
	public ResponseEntity<?> token(@RequestParam(name = "grant_type", required = true) String grantType,
			@RequestParam(name = "code", required = false) String code,
			@RequestParam(name = "redirect_uri", required = false) String redirectUri,
			@RequestParam(name = "client_id", required = false) String appId,
			@RequestParam(name = "refresh_token", required = false) String refreshCode,
			@RequestParam(name = "client_assertion_type", required = false) String clientAssertionType,
			@RequestParam(name = "client_assertion", required = false) String clientAssertion,
			@RequestParam(name = "scope", required = false) String scope, Model model) {

		// Purge any old sessions.
		smartOnFhirSession.purgeOldSession();
		
//		// Alway pass this information so that JSP can route to correct endpoint
//		model.addAttribute("base_url", baseUrl);

		logger.debug("Token Requested:\ngrant_type: " + grantType + "\ncode: " + code + "\nredirect_uri:" + redirectUri
				+ "\nclient_id:" + appId + "\nclient_assertion_type:" + clientAssertionType + "\nclient_assertion:"
				+ clientAssertion + "\n");

		if (!"authorization_code".equals(grantType) && !"refresh_token".equals(grantType)
				&& !"client_credentials".equals(grantType)) {
			OAuth2Error error = new OAuth2Error("unsupported_grant_type",
					"grant type should be authorization_code, refresh_token, or client_credentials");
			return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
		}

		Long now = (new Date()).getTime();
		SmartOnFhirAppEntry smartApp = null;
		SmartOnFhirSessionEntry smartSession = null;

		boolean generateRefershToken = true;

		// If this is refresh token request, we handle it differently,
		if ("refresh_token".equals(grantType)) {
			// if refresh_token does not exist, we return error.
			if (refreshCode == null || refreshCode.isEmpty()) {
				logger.debug("Refresh token does not exist.");
				OAuth2Error error = new OAuth2Error("invalid_request", "refresh token cannot be empty");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

			smartSession = smartOnFhirSession.getSmartOnFhirAppByRefreshToken(refreshCode);
			if (smartSession == null) {
				logger.debug("Session does not exist for the provided refersh token: " + refreshCode);
				OAuth2Error error = new OAuth2Error("invalid_grant", "no session for provided refresh token");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

			appId = smartSession.getAppId();
			code = smartSession.getAuthorizationCode();
			smartApp = smartOnFhirApp.getSmartOnFhirApp(appId);

			// Spec is asking to do the authentication again if we require authentication.
			// As we are just testing, we just check the refresh token.
			String refreshToken = smartSession.getRefreshToken();
			if (!refreshToken.equals(refreshCode)) {
				// Incorrect refresh token.
				logger.debug("Incorrect refresh token (" + refreshCode + ") provided. Correct refresh token:"
						+ refreshToken);
				OAuth2Error error = new OAuth2Error("invalid_grant", "incorrecr refresh token");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

//			if (smartSession.getAccessTokenExpirationDT() != null) {
//				Long expire = smartSession.getAccessTokenExpirationDT().getTime();
//				if (expire <= now) {
//					logger.info("Access Token for session-id: " + smartSession.getSessionId() + " is expired");
//
//					// Expired. 400 respond with invalid_grant
//					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_grant");
//				}
//			}

		} else if ("authorization_code".equals(grantType)) {
			// This is a token request.
			smartApp = smartOnFhirApp.getSmartOnFhirApp(appId, redirectUri);
			if (smartApp == null) {
				// Invalid client-id. We should send with bad request.
				logger.debug("App does not exist for the AppID:" + appId + ", and redirectUri:" + redirectUri);
				OAuth2Error error = new OAuth2Error("invalid_request", "incorrect client-id or redirect uri");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}
			smartSession = smartOnFhirSession.getSmartOnFhirSession(appId, code);
			if (smartSession == null) {
				logger.debug("Session does not exist for the AppID:" + appId + ", and auth code:" + code);
				OAuth2Error error = new OAuth2Error("invalid_grant", "incorrecr authorization code");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

			// Check if we are in the authorized time window.
			Long expire = smartSession.getAuthCodeExpirationDT().getTime();
			if (expire <= now) {
				logger.info("Authorization for session-id: " + smartSession.getSessionId() + " is expired");

				// Expired. 400 respond with invalid_grant
				OAuth2Error error = new OAuth2Error("invalid_grant", "authorization code is expired");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}
		} else {
			// client_credentials
			// 1. Check if this has correct assertion type.
			if (!"urn:ietf:params:oauth:client-assertion-type:jwt-bearer".equals(clientAssertionType)) {
				// incorrect type received.
				// Invalid client-id. We should send with bad request.
				logger.debug("client_assertion_type must be urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
				OAuth2Error error = new OAuth2Error("invalid_client", "incorrecr client_assertion_type");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

			// 2. Validate the JWT and get claims.
			Jws<Claims> jwsClaims;
			try {
				jwsClaims = JwtUtil.getJWTClaims(clientAssertion);
			} catch (SignatureException e) {
				logger.debug("JWT failed to get validated: " + e.getMessage());
				OAuth2Error error = new OAuth2Error("invalid_client", e.getMessage());
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

			// 3. check iss == client_id
			Claims claims = jwsClaims.getBody();
			JwsHeader header = jwsClaims.getHeader();
			String cilentId = claims.getIssuer();

			List<JwkSetEntry> jwkSets = jwkSet.getJwkSetByAppId(cilentId);
			if (jwkSets.size() == 0) {
				// Invalid client-id. We should send with bad request.
				logger.debug("JWK does not exist for the client-id:" + appId + ", and jws:" + jwsClaims.toString());
				OAuth2Error error = new OAuth2Error("invalid_client", "incorrect client-id");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

			smartApp = smartOnFhirApp.getSmartOnFhirApp(cilentId);
			if (smartApp == null) {
				// Invalid client-id. We should send with bad request.
				logger.debug("App does not exist for the client-id:" + appId + ", and jws:" + jwsClaims.toString());
				OAuth2Error error = new OAuth2Error("invalid_client", "incorrect client-id");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

			// Choose one that for this JWT
			String alg = header.getAlgorithm();
			JwkSetEntry jwkSetEntry = JwtUtil.matchJwkSetEntry(jwkSets, alg);

			// 4. check if it's expired.
			Date jwtExpiration = claims.getExpiration();
			Integer expTime = jwkSetEntry.getExp();
			Integer jwtExpTime = (int) (jwtExpiration.getTime() / 1000);
			if (jwtExpTime > expTime + 300) { // 5min skew time.
				logger.debug("JWT is expired. JWT Exp=" + jwtExpTime + ". JWK Exp=" + expTime);
				OAuth2Error error = new OAuth2Error("invalid_client", "jwt expired");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

			// JWS is not expired. Now, we check jti to avoid any replay attack.
			String jtiInJwk = jwkSetEntry.getJti();
			String jtiInJwt = claims.getId();
			if (jtiInJwk == jtiInJwt) {
				// smae jti. this is replay.
				logger.debug("jti is same as last one in active period (within exp)");
				OAuth2Error error = new OAuth2Error("invalid_client", "previously encountered jti");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.BAD_REQUEST);
			}

			// Now we are good. Save jit before we proceed.
			jwkSetEntry.setJti(jtiInJwt);
			jwkSet.update(jwkSetEntry);

			// Create a session.
			smartSession = new SmartOnFhirSessionEntry();
			String uuid = getNewUUID();
			smartSession.setSessionId(uuid);
			smartSession.setAppId(cilentId);
			
			// we need to put access token now.
			String myAccessToken = getAToken();			
			if (myAccessToken == null) {
				logger.debug("jti is same as last one in active period (within exp)");
				OAuth2Error error = new OAuth2Error("internal_error", "failed to create a token");
				return new ResponseEntity<OAuth2Error>(error, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			smartSession.setAccessToken(myAccessToken);

			smartOnFhirSession.save(smartSession);
			generateRefershToken = false; // SMART on FHIR backend service do not allow refresh token.
		}

		// It is OK the access token is expired as long as the auth code is not expired.
		// Again, if this is refresh token, we move on.

		// generate access_token if needed.
		String accessToken = smartSession.getAccessToken();
		String refreshToken = smartSession.getRefreshToken();
		Long expiration;
		if (accessToken == null || accessToken.isEmpty()) {
			accessToken = getAToken();
			smartOnFhirSession.putAccessCode(appId, code, accessToken);

			// Add new refresh token as well as the access token is new.
			if (generateRefershToken) {
				refreshToken = getAToken();
				smartOnFhirSession.putRefereshCode(appId, code, refreshToken);
			}
		}
//		else {
//			expiration = (now - expire) / 1000;
//		}

		// We will issue new access token expiration date. Update Access Token Timeout.
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, accessTokenTimeoutMinutes);
		java.sql.Date expiresIn = new java.sql.Date(calendar.getTimeInMillis());
		smartOnFhirSession.updateAccessTokenTimeout(smartSession.getSessionId(), expiresIn);

		expiration = (long) accessTokenTimeoutMinutes * 60;

		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setAccessToken(accessToken);
		tokenResponse.setRefreshToken(refreshToken);
		tokenResponse.setExpiresIn(expiration);
		tokenResponse.setScope(smartApp.getScope());
		tokenResponse.setTokenType("Bearer");

		String authorization_code = smartSession.getAuthorizationCode();
		if (authorization_code != null) {
			String patient = getPatientIdFromJWT(smartSession.getAuthorizationCode());
			if (patient != null && !patient.isEmpty()) {
				tokenResponse.setPatient(patient);
			}
		}

		logger.debug("token: responding with " + tokenResponse.toString());

		return new ResponseEntity<TokenResponse>(tokenResponse, HttpStatus.OK);
//		return tokenResponse.toString();
	}

	@PostMapping(value = "/introspect")
	public ResponseEntity<?> introspect(HttpServletRequest request,
			@RequestParam(name = "token", required = true) String token, Model model) {

		// Check Basic authentication.
		String authReq = request.getHeader("Authorization");
		if (authReq == null || authReq.isEmpty()) {
//			logger.debug("No Authorization Header found in the header.");
//			OAuth2Error error = new OAuth2Error("invalid_client", "No Authorization Header");
//			return new ResponseEntity<OAuth2Error>(error, HttpStatus.UNAUTHORIZED);
			logger.info("Authorization header mising");
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header mising");
		}

		IntrospectResponse introspectResponse = null;
		if (authReq.startsWith("Basic ") || authReq.startsWith("basic ")) {
			String basicKey = authReq.substring(6);
			String base64decoded = new String(Base64.decodeBase64(basicKey));
			if (!authBasic.equals(base64decoded)) {
				logger.info("Basic Authorization Failed: " + base64decoded + " requested");
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Basic Authorization Failed");
			}
		} else {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization Header");
		}

		Long now = (new Date()).getTime();

		String authBearer = System.getenv("AUTH_BEARER");
		if (authBearer != null && !authBearer.isEmpty()) {
			logger.debug("local bearer " + token);
			if (token.equals(authBearer.trim())) {
				// This is local bearer request. We allow with only Read.
				// And we always give a new 5min expiration time, which means it never expires.
				introspectResponse = new IntrospectResponse(true, "launch profile openid online_access user/*.*");
				introspectResponse.setExp((now / 1000) + accessTokenTimeoutMinutes * 60);
				introspectResponse.setTokenType("Bearer");

				return new ResponseEntity<IntrospectResponse>(introspectResponse, HttpStatus.OK);
			}
		}

		SmartOnFhirSessionEntry smartSession = smartOnFhirSession.getSmartOnFhirAppByToken(token);
		if (smartSession == null) {
			// We couldn't find a token. We send 200 OK with set this token inactive. 
			// Put only active key.
			// see Sec 2.2 of RFC 7662
			introspectResponse = new IntrospectResponse();
			introspectResponse.setActive(false);

			return new ResponseEntity<IntrospectResponse>(introspectResponse, HttpStatus.OK);
		}

		SmartOnFhirAppEntry smartApp = smartOnFhirApp.getSmartOnFhirApp(smartSession.getAppId());
		if (smartApp == null) {
			introspectResponse = new IntrospectResponse();
			introspectResponse.setActive(false);

			return new ResponseEntity<IntrospectResponse>(introspectResponse, HttpStatus.OK);
		}

		Long expire = smartSession.getAccessTokenExpirationDT().getTime();
		if (expire <= now) {
			introspectResponse = new IntrospectResponse();
			introspectResponse.setActive(false);

			return new ResponseEntity<IntrospectResponse>(introspectResponse, HttpStatus.OK);
		}

		introspectResponse = new IntrospectResponse(true, smartApp.getScope());
		String authorizationCode = smartSession.getAuthorizationCode();
		if (authorizationCode != null && !authorizationCode.isEmpty()) {
			String patient = getPatientIdFromJWT(authorizationCode);
			if (patient != null && !patient.isEmpty()) {
				introspectResponse.setPatient(patient);
			}
		}
		
		introspectResponse.setExp(expire / 1000);
		introspectResponse.setTokenType("Bearer");

		return new ResponseEntity<IntrospectResponse>(introspectResponse, HttpStatus.OK);
	}

	private String getNewUUID() {
		String uuid = null;

		boolean exists = true;
		while (exists) {
			uuid = UUID.randomUUID().toString();
			exists = smartOnFhirSession.exists(uuid);
		}

		return uuid;
	}

	@PostMapping(value = "/after-auth")
	public ModelAndView afterAuth(@RequestParam(name = "launch", required = false) String launchContext,
			@RequestParam(name = "response_type", required = false) String responseType,
			@RequestParam(name = "client_id", required = false) String clientId,
			@RequestParam(name = "redirect_uri", required = true) String redirectUri,
			@RequestParam(name = "scope", required = false) String scope,
			@RequestParam(name = "aud", required = false) String aud,
			@RequestParam(name = "state", required = false) String state, ModelMap model) {
		String error, errorDesc;

//		// Alway pass this information so that JSP can route to correct endpoint
//		model.addAttribute("base_url", baseUrl);
		model.remove("base_url");

//		if (oauth2attr == null || !oauth2attr.has("client_id") || !oauth2attr.has("redirect_uri")) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
//		}

		if (clientId == null || clientId.isEmpty() || redirectUri == null || redirectUri.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_request");
		}

		SmartOnFhirAppEntry smartApp = smartOnFhirApp.getSmartOnFhirApp(clientId);

		// Check if an authorization already exists for this session.
		// Get all the sessions for this client and see if any session has a same
		// patient id. We already checked scope in authorize().
		boolean createNewSession = true;
		SmartOnFhirSessionEntry sessionEntry = null;
		List<SmartOnFhirSessionEntry> smartSessions = smartOnFhirSession.getSmartOnFhirSessionsByAppId(clientId);
		for (SmartOnFhirSessionEntry entry : smartSessions) {
			if (entry.getAccessToken() != null && entry.getAccessToken().equals(clientId)) {
				String patientInCode = getPatientIdFromJWT(entry.getAuthorizationCode());
				String patientInContext = getPatientFromLaunchContext(launchContext);
				if (patientInCode != null && !patientInCode.isEmpty() && patientInContext != null
						&& !patientInContext.isEmpty()) {
					if (patientInCode.equals(patientInContext)) {
						sessionEntry = entry;
						createNewSession = false;
						logger.debug("There is an existing session for this patient, " + patientInCode
								+ ", with client-id: " + clientId);
						break;
					}
				}
			}
		}

		// Create a session for this authorization.
		if (sessionEntry == null) {
			String code = generateJWT(launchContext, scope, smartApp);
			if (code == null || code.isEmpty()) {
				try {
					error = "server_error";
					errorDesc = encodeValue("Internal Server Error");
					model.addAttribute("error", error);
					model.addAttribute("error_description", errorDesc);
					return new ModelAndView("redirect:" + smartApp.getRedirectUri(), model);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", e);
				}
			}

			sessionEntry = new SmartOnFhirSessionEntry();
			String uuid = getNewUUID();
			sessionEntry.setSessionId(uuid);
			sessionEntry.setAuthorizationCode(code);
			sessionEntry.setAppId(smartApp.getAppId());
		}
		sessionEntry.setState(state);

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, accessTokenTimeoutMinutes);
		java.sql.Date expiresIn = new java.sql.Date(calendar.getTimeInMillis());
		sessionEntry.setAuthCodeExpirationDT(expiresIn);

		if (createNewSession) {
			smartOnFhirSession.save(sessionEntry);
		} else {
			smartOnFhirSession.update(sessionEntry);
		}

		model.addAttribute("code", sessionEntry.getAuthorizationCode());
		model.addAttribute("state", state);
		return new ModelAndView("redirect:" + smartApp.getRedirectUri(), model);
	}

	@GetMapping(value = "/app-create")
	public String appCreate(Model model) {
		String uuid = "";
		boolean exists = true;

		// Alway pass this information so that JSP can route to correct endpoint
		model.addAttribute("base_url", baseUrl);

		while (exists) {
			uuid = UUID.randomUUID().toString();
			exists = smartOnFhirApp.exists(uuid);
		}

		model.addAttribute("client_id", uuid);

		return "app_create";
	}

	private void populateModel(Model model, SmartOnFhirAppEntry appEntry, JwkSetEntry jwkSetEntry) {
		model.addAttribute("client_id", appEntry.getAppId());
		model.addAttribute("scope", appEntry.getScope());
		model.addAttribute("app_type", appEntry.getAppType());
		model.addAttribute("redirect_uri", appEntry.getRedirectUri());
		model.addAttribute("launch_uri", appEntry.getLaunchUri());
		model.addAttribute("app_name", appEntry.getAppName());
		if ("System".equals(appEntry.getAppType()) && jwkSetEntry != null) {
			model.addAttribute("kty", jwkSetEntry.getKty());
			model.addAttribute("kid", jwkSetEntry.getKid());
			model.addAttribute("exp", jwkSetEntry.getExp());
			model.addAttribute("public_key", jwkSetEntry.getPublicKey());
		} else {
			model.addAttribute("kty", "");
			model.addAttribute("kid", "");
			model.addAttribute("exp", "");
			model.addAttribute("public_key", "");
		}
	}

	private String makeScope(String appType, String user_condition_r, String user_documentreference_r,
			String user_encounter_r, String user_medicationstatement_r, String user_medicationrequest_r,
			String user_observation_r, String user_patient_r, String user_procedure_r, String patient_condition_r,
			String patient_documentreference_r, String patient_encounter_r, String patient_medicationstatement_r,
			String patient_medicationrequest_r, String patient_observation_r, String patient_patient_r,
			String patient_procedure_r, String user_condition_w, String user_documentreference_w,
			String user_encounter_w, String user_medicationstatement_w, String user_medicationrequest_w,
			String user_observation_w, String user_patient_w, String user_procedure_w, String patient_condition_w,
			String patient_documentreference_w, String patient_encounter_w, String patient_medicationstatement_w,
			String patient_medicationrequest_w, String patient_observation_w, String patient_patient_w,
			String patient_procedure_w) {
		String scope = "launch profile openid online_access ";
		if ("Patient".equals(appType))
			scope += "launch/patient ";

		// read
		if (user_condition_r != null)
			scope += "user/Condition.read ";
		if (user_documentreference_r != null)
			scope += "user/DocumentReference.read ";
		if (user_encounter_r != null)
			scope += "user/Encounter.read ";
		if (user_medicationstatement_r != null)
			scope += "user/MedicationStatement.read ";
		if (user_medicationrequest_r != null)
			scope += "user/MedicationRequest.read ";
		if (user_observation_r != null)
			scope += "user/Observation.read ";
		if (user_patient_r != null)
			scope += "user/Patient.read ";
		if (user_procedure_r != null)
			scope += "user/Procedure.read ";
		if (patient_condition_r != null)
			scope += "patient/Condition.read ";
		if (patient_documentreference_r != null)
			scope += "patient/DocumentReference.read ";
		if (patient_encounter_r != null)
			scope += "patient/Encounter.read ";
		if (patient_medicationstatement_r != null)
			scope += "patient/MedicationStatement.read ";
		if (patient_medicationrequest_r != null)
			scope += "patient/MedicationRequest.read ";
		if (patient_observation_r != null)
			scope += "patient/Observation.read ";
		if (patient_patient_r != null)
			scope += "patient/Patient.read ";
		if (patient_procedure_r != null)
			scope += "patient/Procedure.read ";

		// write
		if (user_condition_w != null)
			scope += "user/Condition.write ";
		if (user_documentreference_w != null)
			scope += "user/DocumentReference.write ";
		if (user_encounter_w != null)
			scope += "user/Encounter.write ";
		if (user_medicationstatement_w != null)
			scope += "user/MedicationStatement.write ";
		if (user_medicationrequest_w != null)
			scope += "user/MedicationRequest.write ";
		if (user_observation_w != null)
			scope += "user/Observation.write ";
		if (user_patient_w != null)
			scope += "user/Patient.write ";
		if (user_procedure_w != null)
			scope += "user/Procedure.write ";
		if (patient_condition_w != null)
			scope += "patient/Condition.write ";
		if (patient_documentreference_w != null)
			scope += "patient/DocumentReference.write ";
		if (patient_encounter_w != null)
			scope += "patient/Encounter.write ";
		if (patient_medicationstatement_w != null)
			scope += "patient/MedicationStatement.write ";
		if (patient_medicationrequest_w != null)
			scope += "patient/MedicationRequest.write ";
		if (patient_observation_w != null)
			scope += "patient/Observation.write ";
		if (patient_patient_w != null)
			scope += "patient/Patient.write ";
		if (patient_procedure_w != null)
			scope += "patient/Procedure.write ";

		return scope.trim();
	}

	private String makeScope(String selectedScopes) {
		// Just do the sanity check. And, remove any duplicates.
		String[] scopes = selectedScopes.split(" ");
		String scope = "";

		for (String scope_ : scopes) {
			scope_ = scope_.trim().replaceAll("\\s+", " ");
			if (!scope.contains(scope_)) {
				scope += " " + scope_;
			}
		}

		return scope.trim().replaceAll("\\s+", " ");
	}

	@RequestMapping(value = "/app-new")
	public String appNew(@RequestParam(name = "client_id", required = true) String appId,
			@RequestParam(name = "app_type", required = true) String appType,
			@RequestParam(name = "redirect_uri", required = true) String redirectUri,
			@RequestParam(name = "launch_uri", required = false) String launchUri,
			@RequestParam(name = "app_name", required = true) String appName,
			@RequestParam(name = "public_key", required = false) String publicKey,
			@RequestParam(name = "selected_scopes", required = true) String selectedScopes,
			@RequestParam(name = "kty", required = false) String kty,
			@RequestParam(name = "kid", required = false) String kid,
			@RequestParam(name = "exp", required = false) String exp,
			@RequestParam(name = "jwk_raw", required = false) String jwkRaw,
			@RequestParam(name = "user_condition_r", required = false) String user_condition_r,
			@RequestParam(name = "user_documentreference_r", required = false) String user_documentreference_r,
			@RequestParam(name = "user_encounter_r", required = false) String user_encounter_r,
			@RequestParam(name = "user_medicationstatement_r", required = false) String user_medicationstatement_r,
			@RequestParam(name = "user_medicationrequest_r", required = false) String user_medicationrequest_r,
			@RequestParam(name = "user_observation_r", required = false) String user_observation_r,
			@RequestParam(name = "user_patient_r", required = false) String user_patient_r,
			@RequestParam(name = "user_procedure_r", required = false) String user_procedure_r,
			@RequestParam(name = "patient_condition_r", required = false) String patient_condition_r,
			@RequestParam(name = "patient_documentreference_r", required = false) String patient_documentreference_r,
			@RequestParam(name = "patient_encounter_r", required = false) String patient_encounter_r,
			@RequestParam(name = "patient_medicationstatement_r", required = false) String patient_medicationstatement_r,
			@RequestParam(name = "patient_medicationrequest_r", required = false) String patient_medicationrequest_r,
			@RequestParam(name = "patient_observation_r", required = false) String patient_observation_r,
			@RequestParam(name = "patient_patient_r", required = false) String patient_patient_r,
			@RequestParam(name = "patient_procedure_r", required = false) String patient_procedure_r,
			@RequestParam(name = "user_condition_w", required = false) String user_condition_w,
			@RequestParam(name = "user_documentreference_w", required = false) String user_documentreference_w,
			@RequestParam(name = "user_encounter_w", required = false) String user_encounter_w,
			@RequestParam(name = "user_medicationstatement_w", required = false) String user_medicationstatement_w,
			@RequestParam(name = "user_medicationrequest_w", required = false) String user_medicationrequest_w,
			@RequestParam(name = "user_observation_w", required = false) String user_observation_w,
			@RequestParam(name = "user_patient_w", required = false) String user_patient_w,
			@RequestParam(name = "user_procedure_w", required = false) String user_procedure_w,
			@RequestParam(name = "patient_condition_w", required = false) String patient_condition_w,
			@RequestParam(name = "patient_documentreference_w", required = false) String patient_documentreference_w,
			@RequestParam(name = "patient_encounter_w", required = false) String patient_encounter_w,
			@RequestParam(name = "patient_medicationstatement_w", required = false) String patient_medicationstatement_w,
			@RequestParam(name = "patient_medicationrequest_w", required = false) String patient_medicationrequest_w,
			@RequestParam(name = "patient_observation_w", required = false) String patient_observation_w,
			@RequestParam(name = "patient_patient_w", required = false) String patient_patient_w,
			@RequestParam(name = "patient_procedure_w", required = false) String patient_procedure_w, Model model) {

		// Alway pass this information so that JSP can route to correct endpoint
		model.addAttribute("base_url", baseUrl);

		String scope;
		if (selectedScopes == null || selectedScopes.isEmpty()) {
			scope = makeScope(appType, user_condition_r, user_documentreference_r, user_encounter_r,
					user_medicationstatement_r, user_medicationrequest_r, user_observation_r, user_patient_r,
					user_procedure_r, patient_condition_r, patient_documentreference_r, patient_encounter_r,
					patient_medicationstatement_r, patient_medicationrequest_r, patient_observation_r,
					patient_patient_r, patient_procedure_r, user_condition_w, user_documentreference_w,
					user_encounter_w, user_medicationstatement_w, user_medicationrequest_w, user_observation_w,
					user_patient_w, user_procedure_w, patient_condition_w, patient_documentreference_w,
					patient_encounter_w, patient_medicationstatement_w, patient_medicationrequest_w,
					patient_observation_w, patient_patient_w, patient_procedure_w);
		} else {
			scope = makeScope(selectedScopes);
		}

		SmartOnFhirAppEntry appEntry = new SmartOnFhirAppEntry();
		appEntry.setAppId(appId);
		appEntry.setScope(scope.trim());
		appEntry.setAppType(appType);
		appEntry.setRedirectUri(redirectUri);
		appEntry.setLaunchUri(launchUri);
		appEntry.setAppName(appName);

		smartOnFhirApp.save(appEntry);

		JwkSetEntry jwkSetEntry = null;
		if ("System".equals(appType)) {
			jwkSetEntry = new JwkSetEntry();
			jwkSetEntry.setAppId(appId);
			jwkSetEntry.setKty(kty);
			jwkSetEntry.setKid(kid);
			if (exp != null && !exp.isEmpty()) {
				jwkSetEntry.setExp(Integer.valueOf(exp));
			} else {
				jwkSetEntry.setExp(1902494733);
			}
			jwkSetEntry.setPublicKey(publicKey);
			jwkSetEntry.setJwkRaw(jwkRaw);

			jwkSet.save(jwkSetEntry);
		}

		populateModel(model, appEntry, jwkSetEntry);

		return "app_view";
	}

	@RequestMapping(value = "/app-update")
	public String appUpdate(@RequestParam(name = "client_id", required = true) String appId,
			@RequestParam(name = "app_type", required = true) String appType,
			@RequestParam(name = "redirect_uri", required = true) String redirectUri,
			@RequestParam(name = "launch_uri", required = false) String launchUri,
			@RequestParam(name = "app_name", required = true) String appName,
			@RequestParam(name = "public_key", required = true) String publicKey,
			@RequestParam(name = "selected_scopes", required = true) String selectedScopes,
			@RequestParam(name = "kty", required = false) String kty,
			@RequestParam(name = "kid", required = false) String kid,
			@RequestParam(name = "exp", required = false) String exp,
			@RequestParam(name = "jwk_raw", required = false) String jwkRaw,
			@RequestParam(name = "user_condition_r", required = false) String user_condition_r,
			@RequestParam(name = "user_documentreference_r", required = false) String user_documentreference_r,
			@RequestParam(name = "user_encounter_r", required = false) String user_encounter_r,
			@RequestParam(name = "user_medicationstatement_r", required = false) String user_medicationstatement_r,
			@RequestParam(name = "user_medicationrequest_r", required = false) String user_medicationrequest_r,
			@RequestParam(name = "user_observation_r", required = false) String user_observation_r,
			@RequestParam(name = "user_patient_r", required = false) String user_patient_r,
			@RequestParam(name = "user_procedure_r", required = false) String user_procedure_r,
			@RequestParam(name = "patient_condition_r", required = false) String patient_condition_r,
			@RequestParam(name = "patient_documentreference_r", required = false) String patient_documentreference_r,
			@RequestParam(name = "patient_encounter_r", required = false) String patient_encounter_r,
			@RequestParam(name = "patient_medicationstatement_r", required = false) String patient_medicationstatement_r,
			@RequestParam(name = "patient_medicationrequest_r", required = false) String patient_medicationrequest_r,
			@RequestParam(name = "patient_observation_r", required = false) String patient_observation_r,
			@RequestParam(name = "patient_patient_r", required = false) String patient_patient_r,
			@RequestParam(name = "patient_procedure_r", required = false) String patient_procedure_r,
			@RequestParam(name = "user_condition_w", required = false) String user_condition_w,
			@RequestParam(name = "user_documentreference_w", required = false) String user_documentreference_w,
			@RequestParam(name = "user_encounter_w", required = false) String user_encounter_w,
			@RequestParam(name = "user_medicationstatement_w", required = false) String user_medicationstatement_w,
			@RequestParam(name = "user_medicationrequest_w", required = false) String user_medicationrequest_w,
			@RequestParam(name = "user_observation_w", required = false) String user_observation_w,
			@RequestParam(name = "user_patient_w", required = false) String user_patient_w,
			@RequestParam(name = "user_procedure_w", required = false) String user_procedure_w,
			@RequestParam(name = "patient_condition_w", required = false) String patient_condition_w,
			@RequestParam(name = "patient_documentreference_w", required = false) String patient_documentreference_w,
			@RequestParam(name = "patient_encounter_w", required = false) String patient_encounter_w,
			@RequestParam(name = "patient_medicationstatement_w", required = false) String patient_medicationstatement_w,
			@RequestParam(name = "patient_medicationrequest_w", required = false) String patient_medicationrequest_w,
			@RequestParam(name = "patient_observation_w", required = false) String patient_observation_w,
			@RequestParam(name = "patient_patient_w", required = false) String patient_patient_w,
			@RequestParam(name = "patient_procedure_w", required = false) String patient_procedure_w, Model model) {

		// Alway pass this information so that JSP can route to correct endpoint
		model.addAttribute("base_url", baseUrl);

		String scope;
		if (selectedScopes == null || selectedScopes.isEmpty()) {
			scope = makeScope(appType, user_condition_r, user_documentreference_r, user_encounter_r,
					user_medicationstatement_r, user_medicationrequest_r, user_observation_r, user_patient_r,
					user_procedure_r, patient_condition_r, patient_documentreference_r, patient_encounter_r,
					patient_medicationstatement_r, patient_medicationrequest_r, patient_observation_r,
					patient_patient_r, patient_procedure_r, user_condition_w, user_documentreference_w,
					user_encounter_w, user_medicationstatement_w, user_medicationrequest_w, user_observation_w,
					user_patient_w, user_procedure_w, patient_condition_w, patient_documentreference_w,
					patient_encounter_w, patient_medicationstatement_w, patient_medicationrequest_w,
					patient_observation_w, patient_patient_w, patient_procedure_w);
		} else {
			scope = makeScope(selectedScopes);
		}

		SmartOnFhirAppEntry appEntry = smartOnFhirApp.getSmartOnFhirApp(appId);
		if (appEntry == null) {
			model.addAttribute("error", "Invaid client Id");
			return "error";
		}

		appEntry.setAppId(appId);
		appEntry.setScope(scope);
		appEntry.setAppType(appType);
		appEntry.setRedirectUri(redirectUri);
		appEntry.setLaunchUri(launchUri);
		appEntry.setAppName(appName);

		smartOnFhirApp.update(appEntry);

		JwkSetEntry jwkSetEntry = null;
		if ("System".equals(appType)) {
			jwkSetEntry = new JwkSetEntry();
			jwkSetEntry.setAppId(appId);
			jwkSetEntry.setKty(kty);
			jwkSetEntry.setKid(kid);
			if (exp != null && !exp.isEmpty()) {
				jwkSetEntry.setExp(Integer.valueOf(exp));
			} else {
				jwkSetEntry.setExp(1902494733);
			}
			jwkSetEntry.setPublicKey(publicKey);
			jwkSetEntry.setJwkRaw(jwkRaw);

			jwkSet.update(jwkSetEntry);
		}

		populateModel(model, appEntry, jwkSetEntry);

		return "app_view";
	}

	@GetMapping(value = "/app-edit")
	public String appEdit(@RequestParam(name = "client_id", required = true) String appId, Model model) {

		// Alway pass this information so that JSP can route to correct endpoint
		model.addAttribute("base_url", baseUrl);

		SmartOnFhirAppEntry appEntry = smartOnFhirApp.getSmartOnFhirApp(appId);
		if (appEntry == null) {
			model.addAttribute("error", "Invaid client Id");
			return "error";
		}

		List<JwkSetEntry> jwkSetEntries = jwkSet.getJwkSetByAppId(appId);
		JwkSetEntry jwkSetEntry = null;
		if (jwkSetEntries.size() > 0) {
			jwkSetEntry = jwkSetEntries.get(0);
		}

		populateModel(model, appEntry, jwkSetEntry);

		return "app_edit";
	}

	@GetMapping(value = "/app-view")
	public String appView(@RequestParam(name = "client_id", required = true) String appId, Model model) {
		SmartOnFhirAppEntry appEntry = smartOnFhirApp.getSmartOnFhirApp(appId);
		List<JwkSetEntry> jwkSetEntries = jwkSet.getJwkSetByAppId(appId);
		JwkSetEntry jwkSetEntry = null;
		if (jwkSetEntries.size() > 0) {
			jwkSetEntry = jwkSetEntries.get(0);
		}

		populateModel(model, appEntry, jwkSetEntry);

		// Alway pass this information so that JSP can route to correct endpoint
		model.addAttribute("base_url", baseUrl);

		return "app_view";
	}

	@DeleteMapping(value = "/app-delete")
	public String appDelete(@RequestParam(name = "client_id", required = true) String appId, Model model) {
		jwkSet.delete(appId);
		smartOnFhirSession.deleteByAppId(appId);
		smartOnFhirApp.delete(appId);

		// Alway pass this information so that JSP can route to correct endpoint
		model.addAttribute("base_url", baseUrl);

		return goIndex(model);
	}

	@GetMapping(value = "/app-launch")
	public ModelAndView appLaunch(@RequestParam(name = "client_id", required = true) String appId,
			@RequestParam(name = "patient_id", required = true) String patientId, ModelMap model) {

		// Alway pass this information so that JSP can route to correct endpoint
//		model.addAttribute("base_url", baseUrl);
		model.remove("base_url");

		SmartOnFhirAppEntry smartApp = smartOnFhirApp.getSmartOnFhirApp(appId);

		// Create launch context
		JSONObject launchContextJson = new JSONObject();
		launchContextJson.put("patient", patientId);

		JSONObject launchContextEncoded = SmartLauncherCodec.encode(launchContextJson);

		String launchContext = Base64.encodeBase64String(launchContextEncoded.toString().getBytes());
		String iss = System.getenv("SERVERBASE_URL");
		if (iss == null || iss.isEmpty()) {
			iss = "http://localhost:8080/fhir";
		}

		model.addAttribute("iss", iss);
		model.addAttribute("launch", launchContext);
		return new ModelAndView("redirect:" + smartApp.getLaunchUri(), model);
	}

//	@GetMapping(value = "/register")
//	public String register(@RequestParam(name = "client_id", required = false) String appId,
//			@RequestParam(name = "scope", required = false) String scope,
//			@RequestParam(name = "type", required = false) String type,
//			@RequestParam(name = "redirect_uri", required = false) String redirectUri,
//			@RequestParam(name = "launch_uri", required = false) String launchUri,
//			@RequestParam(name = "app_name", required = false) String appName, Model model) {
//
//		if (appId == null || !appId.isEmpty()) {
//			String uuid = "";
//			boolean exists = true;
//			while (exists) {
//				uuid = UUID.randomUUID().toString();
//				exists = smartOnFhirApp.exists(uuid);
//			}
//
//			model.addAttribute("client_id", uuid);
//
//			return "app";
//		}
//
//		SmartOnFhirAppEntry appEntry = new SmartOnFhirAppEntry();
//		appEntry.setAppId(appId);
//		appEntry.setAppName(appName);
//		appEntry.setAppType(type);
//		appEntry.setScope(scope);
//		appEntry.setRedirectUri(redirectUri);
//		appEntry.setLaunchUri(launchUri);
//
//		smartOnFhirApp.save(appEntry);
//
//		model.addAttribute("app_entry", appEntry);
//		return "portal";
//	}

	@GetMapping(value = "")
	public String goIndex(Model model) {
		// Alway pass this information so that JSP can route to correct endpoint
		model.addAttribute("base_url", baseUrl);

		List<SmartOnFhirAppEntry> appEntries = smartOnFhirApp.get();

		SmartOnFhirAppListContainer appList = new SmartOnFhirAppListContainer();
		appList.setAppEntries(appEntries);
		model.addAttribute("appList", appList);

		return "index";
	}

}
