package edu.gatech.chai.fhir.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;

/***
 * AuthenticationInterceptor
 * - Basic Auth: checks if the user credential is valid
 * - Bearer Auth: checks if the bearer token is valid
 */
@Interceptor
public class AuthenticationInterceptor {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private String authNBasic;
	private String authNBearer;

    public AuthenticationInterceptor() {
		String authBasicEnv = System.getenv("AUTH_BASIC");
		if (authBasicEnv != null && !authBasicEnv.isEmpty()) {
			setAuthNBasic(authBasicEnv);
		} else {
			setAuthNBasic("client:secret");
		}
    }

    private void setAuthNBasic(String authNBasic) {
        this.authNBasic = authNBasic;
    }

    private Object getAuthNBasic() {
        return this.authNBasic;
    }

    private void setAuthNBearer(String authNBearer) {
        this.authNBearer = authNBearer;
    }

    private Object getAuthNBearer() {
        return this.authNBearer;
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

	public static boolean isBearerAuth(String authHeader) {
		if (authHeader == null || authHeader.isEmpty() || authHeader.length() < 6) {
			AuthenticationException ex = new AuthenticationException(Msg.code(642) + "Mission or Invalid Authorization Header");
			throw ex.addAuthenticateHeaderForRealm("HAPIFHIRonFhirbase");
		}

		// Check if basic auth.
		String prefix = authHeader.substring(0, 7);
		if ("bearer ".equalsIgnoreCase(prefix)) {
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

		if ("None".equals(getAuthNBasic())) {
			// We turned off the authorization.
			return true;
		}
		
		String authHeader = theRequest.getHeader("Authorization");
		if (AuthenticationInterceptor.isBasicAuth(authHeader)) {
			String[] basicCredential = authNBasic.split(":");
			if (basicCredential.length != 2) {
				AuthenticationException ex = new AuthenticationException("Basic Authorization Setup Incorrectly");
				ex.addAuthenticateHeaderForRealm("HAPIFHIRonFhirbase");
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
			ex.addAuthenticateHeaderForRealm("HAPIFHIRonFhirbase");
			throw ex;
		} else if (isBearerAuth(authHeader)) {
			// connect to the token auth server to verify the signature
			// Get token in auth header - "bearer token_string"
			String token = null;
			try {
				token = authHeader.substring(7);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (token == null || token.isBlank()) {
				logger.debug("Bearer token is either null or blank.");
				return false;
			}
			return true;
		} else {
			AuthenticationException ex = new AuthenticationException("No Valid Authorization Header Found");
			ex.addAuthenticateHeaderForRealm("HAPIFHIRonFhirbase");
			throw ex;
		}
    }
}