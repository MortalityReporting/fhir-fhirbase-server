package edu.gatech.chai.fhir.security;

import java.util.List;

import org.hl7.fhir.r4.model.IdType;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

public class OAuthAuthorizationInterceptor extends AuthorizationInterceptor {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OAuthAuthorizationInterceptor.class);

    public static boolean isBearerAuth(String authHeader) {
		if (authHeader == null || authHeader.isEmpty() || authHeader.length() < 6) {
			AuthenticationException ex = new AuthenticationException(Msg.code(642) + "Mission or Invalid Authorization Header");
			ex.addAuthenticateHeaderForRealm("HAPIFHIRonFhirbase");
			throw ex;
		}

		// Check if basic auth.
		String prefix = authHeader.substring(0, 7);
		if ("bearer ".equalsIgnoreCase(prefix)) {
			return true;
		}

		return false;
    }

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

        // Get the token from the header.
        String authHeader = theRequestDetails.getHeader("Authorization");

        if (BasicAuthenticationInterceptor.isBasicAuth(authHeader)) {
            // Basic Auth must be examined already. At this point, if the basic authorization
            // exists, then this must be authenticated. And, basic auth in Bluejay is full access.
            return new RuleBuilder().allowAll().build();
        }

        if (OAuthAuthorizationInterceptor.isBearerAuth(authHeader)) {
            // Examine the bearer token to build the rule.
            String token = authHeader.substring(7);
            
            // validate the token.
            
            return new RuleBuilder()
                    .allow()
                    .read()
                    .allResources()
                    .inCompartment("Patient", new IdType(1L))
                    .andThen()
                    .allow()
                    .write()
                    .allResources()
                    .inCompartment("Patient", new IdType(1L))
                    .andThen()
                    .denyAll()
                    .build();
        } 

        // By default, the access should be rejected at this point.
        return new RuleBuilder().denyAll().build();
    }
}
