package edu.gatech.chai.fhir.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;

@Interceptor
public class BasicAuthenticationInterceptor {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BasicAuthenticationInterceptor.class);

    private String authBasic;

    public BasicAuthenticationInterceptor() {
		String authBasicEnv = System.getenv("AUTH_BASIC");
		if (authBasicEnv != null && !authBasicEnv.isEmpty()) {
			setAuthBasic(authBasicEnv);
		} else {
			setAuthBasic("client:secret");
		}
    }

    private void setAuthBasic(String authBasicEnv) {
        this.authBasic = authBasicEnv;
    }

    private Object getAuthBasic() {
        return authBasic;
    }

	public static boolean isBasicAuth(String authHeader) {
		if (authHeader == null || authHeader.isEmpty() || authHeader.length() < 6) {
			AuthenticationException ex = new AuthenticationException(Msg.code(642) + "Mission or Invalid Authorization Header");
			ex.addAuthenticateHeaderForRealm("HAPIFHIRonFhirbase");
			throw ex;
		}

		// Check if basic auth.
		String prefix = authHeader.substring(0, 6);
		if ("basic ".equalsIgnoreCase(prefix)) {
			return true;
		}

		return false;
	}

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, 
        HttpServletResponse theResponse) throws AuthenticationException {

		logger.debug("[OAuth] Request from " + theRequest.getRemoteAddr());

		if (theRequestDetails.getRestOperationType() == RestOperationTypeEnum.METADATA) {
			logger.debug("This is METADATA request.");
			return true;
		}

		if ("None".equals(getAuthBasic())) {
			// We turned off the authorization.
			return true;
		}
		
		String authHeader = theRequest.getHeader("Authorization");
		if (BasicAuthenticationInterceptor.isBasicAuth(authHeader)) {
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
				logger.debug("[Basic Auth] Auth is granted with " + username + " and " + password);
				return true;
			}

			AuthenticationException ex = new AuthenticationException("Incorrect Username and Password");
			ex.addAuthenticateHeaderForRealm("OmopOnFhir");
			throw ex;
		} else if (OAuthAuthorizationInterceptor.isBearerAuth(authHeader)) {
			logger.debug("Bearer token will be handled later");
			return true;
		} else {
			AuthenticationException ex = new AuthenticationException("No Valid Authorization Header Found");
			ex.addAuthenticateHeaderForRealm("OmopOnFhir");
			throw ex;
		}
    }
}