package edu.gatech.chai.fhir.security;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilderRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilderRuleOp;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilderRuleOpClassifier;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

public class OAuthAuthorizationInterceptor extends AuthorizationInterceptor {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OAuthAuthorizationInterceptor.class);

    @Autowired
    JwtDecoder jwtDecoder;
    
    public OAuthAuthorizationInterceptor() {

    }

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

        // Get the token from the header.
        String authHeader = theRequestDetails.getHeader("Authorization");

        if (AuthenticationInterceptor.isBasicAuth(authHeader)) {
            // Basic Auth must be examined already. At this point, if the basic authorization
            // exists, then this must be authenticated. And, basic auth in Bluejay is full access.
            return new RuleBuilder().allowAll().build();
        }

        if (AuthenticationInterceptor.isBearerAuth(authHeader)) {
            // Examine the bearer token to build the rule.
            String token = authHeader.substring(7).trim();

            // validate and decode the token
            Jwt jwt = jwtDecoder.decode(token);
            Map<String, Object> claims = jwt.getClaims();
            String scopes = (String) claims.get("scope");

            if (scopes != null && !scopes.isBlank()) {
                String[] _scopes = scopes.trim().split(" ");
                RuleBuilder ruleBuilder = new RuleBuilder();
                for (String scope : _scopes) {
                    String[] scope_details = scope.split(("/"));
                    if (scope_details.length != 2) {
                        // wrong format.
                        logger.warn("Scope format is wrong. Must have '/'.: " + scope);
                        continue;
                    }

                    String scopeLevel = scope_details[0];
                    String[] resourceAccess = scope_details[1].split(".");
                    if (resourceAccess.length != 2) {
                        // wrong format.
                        logger.warn("Scope format is wrong. Must have '.' for resource type and access.: " + scope);
                        continue;
                    }
                    String resourceType = resourceAccess[0];
                    String access = resourceAccess[1];
                    
                    String[] accessParams = resourceAccess[1].split("?");
                    if (accessParams.length > 1) {
                        access = accessParams[0];

                        // We are not yet supporting param=value part. So, we ignore this.
                        // https://www.hl7.org/fhir/smart-app-launch/scopes-and-launch-context.html#fhir-resource-scope-syntax
                    }

                    IAuthRuleBuilderRule allow = ruleBuilder.allow();
                    IAuthRuleBuilderRuleOp allowOp = null;
                    if (access.contains("r") || access.contains("s") || "read".equals(access) || "*".equals(access)) {
                        allowOp = allow.read();
                    } 
                    
                    if (access.contains("c") || "write".equals(access) || "*".equals(access)) {
                        allowOp = allow.create();
                    } 
                    
                    if (access.contains("u") || "write".equals(access) || "*".equals(access)) {
                        allowOp = allow.write();
                    } 
                    
                    if (access.contains("d") || "write".equals(access) || "*".equals(access)) {
                        allowOp = allow.delete();
                    }

                    if (allowOp == null) {
                        logger.warn ("access in scope has unrecozed symbol");
                        continue; // access not recognized.
                    }

                    // Which resource?
                    IAuthRuleBuilderRuleOpClassifier builderRuleOpClassifier = allowOp.resourcesOfType(resourceType);

                    // We can then add inCompartment to limit this access to a certain Patient. 
                    // But, we do not implement Introspect API for the token. So, there is no way
                    // we cannot find which patient(s) this use is authorized. We allow all patients. 

                    builderRuleOpClassifier.withAnyId().andThen();
                }

                return ruleBuilder.build();

                // ruleBuilder.allow();
                // return new RuleBuilder()
                //     .allow()
                //     .read()
                //     .allResources()
                //     .inCompartment("Patient", new IdType(1L))
                //     .andThen()
                //     .allow()
                //     .write()
                //     .allResources()
                //     .inCompartment("Patient", new IdType(1L))
                //     .andThen()
                //     .denyAll()
                //     .build();
            }
        } 

        // By default, the access should be rejected at this point.
        return new RuleBuilder().denyAll().build();
    }
}
