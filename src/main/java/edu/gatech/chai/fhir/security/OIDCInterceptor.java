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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
//import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;

/**
 * @author MC142
 *
 */

public class OIDCInterceptor extends InterceptorAdapter {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(OIDCInterceptor.class);

	private String introspectUrl;
	private String readOnly;
	private String authBasic;
	private String authBearer;

	private static String authKeyName = "smartOnFhirAuth";

	public OIDCInterceptor() {
		String url = System.getenv("SMART_INTROSPECTURL");
		if (url != null && !url.isEmpty()) {
			introspectUrl = url;
		} else {
			introspectUrl = "http://localhost:8080/introspect";
		}
		
		// check environment variables now as they may have been updated.
		String readOnlyEnv = System.getenv("FHIR_READONLY");
		if (readOnlyEnv != null && !readOnlyEnv.isEmpty()) {
			setReadOnly(readOnlyEnv);
		} else {
			setReadOnly("True");
		}

		String authBasicEnv = System.getenv("AUTH_BASIC");
		if (authBasicEnv != null && !authBasicEnv.isEmpty()) {
			setAuthBasic(authBasicEnv);
		} else {
			setAuthBasic("client:secret");
		}

		String authBearerEnv = System.getenv("AUTH_BEARER");
		if (authBearerEnv != null && !authBearerEnv.isEmpty()) {
			setAuthBearer(authBearerEnv);
		} else {
			setAuthBearer("12345");
		}
	}

	@Override
	public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
			HttpServletResponse theResponse) throws AuthenticationException {

		ourLog.debug("[OAuth] Request from " + theRequest.getRemoteAddr());
		if (readOnly.equalsIgnoreCase("True")) {
			if (!theRequest.getMethod().equalsIgnoreCase("GET")) {
				RequestTypeEnum[] allowedMethod = new RequestTypeEnum[] { RequestTypeEnum.GET };
				throw new MethodNotAllowedException("Server Running in Read Only", allowedMethod);
//				return false;
			}
		}

		if (theRequestDetails.getRestOperationType() == RestOperationTypeEnum.METADATA) {
			ourLog.debug("This is METADATA request.");

			// Enumeration<String> headerNames = theRequest.getHeaderNames();
			// while (headerNames.hasMoreElements()) {
			// String headerName = headerNames.nextElement();
			// System.out.println(headerName);
			// Enumeration<String> headers = theRequest.getHeaders(headerName);
			// while (headers.hasMoreElements()) {
			// String headerValue = headers.nextElement();
			// System.out.println(" "+headerValue);
			// }
			// }

			// StringBuilder buffer = new StringBuilder();
			// BufferedReader reader;
			// try {
			// reader = theRequest.getReader();
			// String line;
			// while ((line=reader.readLine())!=null) {
			// buffer.append(line);
			// }
			//
			// System.out.println("METADATA request getbody:
			// "+buffer.toString());
			//
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//

			return true;
		}

//		// Quick Hack for request from localhost overlay site.
//		if (localByPass.equalsIgnoreCase("True")) {
//			ourLog.debug("remoteAddress:"+theRequest.getRemoteAddr()+", localAddress:"+theRequest.getLocalAddr());
//			if (theRequest.getRemoteAddr().equalsIgnoreCase("127.0.0.1")
//					|| theRequest.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
//				return true;
//			}
//
//			if (theRequest.getLocalAddr().equalsIgnoreCase(theRequest.getRemoteAddr())) {
//				return true;
//			}
//			
//			// When this is deployed in docker container and/or with some proxies,
//			// we may set manual server address, which caused all traffics forwarded by proxy.
//			// If the match above fails, we may still be coming from local
//			String[] remoteAddrs = theRequest.getRemoteAddr().split("\\.");
//			String[] localAddrs = theRequest.getLocalAddr().split("\\.");
//			if (remoteAddrs.length == 4 && localAddrs.length == 4) {
//				ourLog.debug("remoteAddrs[0]="+remoteAddrs[0]+", remoteAddrs[1]="+remoteAddrs[1]+" , localAddrs[0]="+localAddrs[0]+", localAddrs[1]="+localAddrs[1]);
//				if (remoteAddrs[0].equals(localAddrs[0]) && remoteAddrs[1].equals(localAddrs[1])) {
//					return true;
//				}
//			}
//		}

//		if (authBasic.equalsIgnoreCase("None")) {
//			ourLog.debug("[OAuth] OAuth is disabled. Request from " + theRequest.getRemoteAddr() + "is approved");
//			return true;
//		}

		if ("None".equals(getAuthBasic()) && "None".equals(getAuthBearer())) {
			// We turned off the authorization.
			return true;
		}
		
		String authHeader = theRequest.getHeader("Authorization");
		if (authHeader == null || authHeader.isEmpty() || authHeader.length() < 6) {
			AuthenticationException ex = new AuthenticationException("No or Invalid Authorization Header");
			ex.addAuthenticateHeaderForRealm("OmopOnFhir");
			throw ex;
		}

		// Check if basic auth.
		String prefix = authHeader.substring(0, 6);
		if ("basic ".equalsIgnoreCase(prefix)) {
			String[] basicCredential = authBasic.split(":");
			if (basicCredential.length != 2) {
				AuthenticationException ex = new AuthenticationException("Basic Authorization Setup Incorrectly");
				ex.addAuthenticateHeaderForRealm("OmopOnFhir");
				throw ex;
			}
			String username = basicCredential[0];
			String password = basicCredential[1];

			String base64 = authHeader.substring(6);

			String base64decoded = new String(Base64.decodeBase64(base64));
			String[] parts = base64decoded.split(":");

			if (username.equals(parts[0]) && password.equals(parts[1])) {
				ourLog.debug("[Basic Auth] Auth is granted with " + username + " and " + password);
				return true;
			}

			AuthenticationException ex = new AuthenticationException("Incorrect Username and Password");
			ex.addAuthenticateHeaderForRealm("OmopOnFhir");
			throw ex;
		} else if ("bearer".equalsIgnoreCase(prefix)) {
			// checking Auth
			ourLog.debug("IntrospectURL:" + getIntrospectUrl() + " with Basic " + getAuthBasic());
			Authorization myAuth = new Authorization(getIntrospectUrl(), getAuthBasic());

			String err_msg = myAuth.introspectToken(theRequest);
			if (err_msg.isEmpty() == false) {
				ourLog.debug("IntrospectToken failed with "+err_msg);
				throw new AuthenticationException(err_msg);
			}

			// Now we have a valid access token. Now, check Token type
			if (myAuth.checkBearer() == false) {
				ourLog.debug("IntrospectToken failed. Not Token Bearer");
				throw new AuthenticationException("Not Token Bearer");
			}

			// Check scope.
			// Fine grain checking should be done after request is parsed. Save
			// this auth to smartOnFhir attribute.
			ourLog.debug("Adding auth object to RequestDetails attribute");
			theRequestDetails.setAttribute(OIDCInterceptor.authKeyName, myAuth);

			return true;
//			return myAuth.allowRequest(theRequestDetails);				
		} else {
			AuthenticationException ex = new AuthenticationException("No Valid Authorization Header Found");
			ex.addAuthenticateHeaderForRealm("OmopOnFhir");
			throw ex;
		}

		// for test.
		// String resourceName = theRequestDetails.getResourceName();
		// String resourceOperationType =
		// theRequestDetails.getResourceOperationType().name();
		// System.out.println ("resource:"+resourceName+",
		// resourceOperationType:"+resourceOperationType);

	}

	@Override
	public void incomingRequestPreHandled(RestOperationTypeEnum theOperation, RequestDetails theProcessedRequest) {
		
		ourLog.debug("Request is parsed. Now in pre handled interceptor");
		RequestDetails requestDetails = theProcessedRequest;

		Authorization myAuth = (Authorization) requestDetails.getAttribute(OIDCInterceptor.authKeyName);
		if (myAuth != null) {
			if (myAuth.allowRequest(requestDetails) == false) {
				// Something happened while checking fine grain auth. Throw exception.
				ourLog.debug("Resourcee level scope and operation checking failed");
				throw new AuthenticationException("Resource level scope or operation is not authorized. Check if you are authorized for patient, resource, or read/write");
			}
			ourLog.debug("Request allowed");
		} else {
			ourLog.debug("No Auth object. Fine grain checking disabled");
		}
	}

	public String getAuthBasic() {
		return authBasic;
	}

	public void setAuthBasic(String authBasic) {
		this.authBasic = authBasic;
	}

	public String getAuthBearer() {
		return authBearer;
	}

	public void setAuthBearer(String authBearer) {
		this.authBearer = authBearer;
	}

	public String getIntrospectUrl() {
		return introspectUrl;
	}

	public void setIntrospectUrl(String introspectURL) {
		this.introspectUrl = introspectURL;
	}

//	public String getLocalByPass() {
//		return localByPass;
//	}
//
//	public void setLocalByPass(String localByPass) {
//		this.localByPass = localByPass;
//	}
//
	public String getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(String readOnly) {
		this.readOnly = readOnly;
	}
}
