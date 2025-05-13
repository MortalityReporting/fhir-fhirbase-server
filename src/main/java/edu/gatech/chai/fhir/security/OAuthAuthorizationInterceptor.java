package edu.gatech.chai.fhir.security;

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

public class OAuthAuthorizationInterceptor extends AuthorizationInterceptor {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OAuthAuthorizationInterceptor.class);

    JwtDecoder jwtDecoder;
    
    public OAuthAuthorizationInterceptor() {
    }

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
        if (theRequestDetails.getRestOperationType() == RestOperationTypeEnum.METADATA) {
			logger.debug("When the request is metadata, allowAll.");
			return new RuleBuilder().allow().metadata().build();
		}

        if ("OperationDefinition".equals(theRequestDetails.getResourceName())) {
            logger.debug("When the request is for OperationDefinition, allowAll.");
			return new RuleBuilder().allow().read().resourcesOfType("OperationDefinition").withAnyId().build();
        }

        // Get the token from the header.
        String authHeader = theRequestDetails.getHeader("Authorization");

        if (AuthenticationInterceptor.isBasicAuth(authHeader)) {
            // Basic Auth must be examined already. At this point, if the basic authorization
            // exists, then this must be authenticated. And, basic auth in Bluejay is full access.
            return new RuleBuilder().allowAll().build();
        }

        if (this.jwtDecoder == null) {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.scan("edu.gatech.chai.fhir.security");
            context.refresh();

            jwtDecoder = context.getBean(JwtDecoder.class);
            context.close();
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

                // We have FHIR operations. We allow all operations for now.
                RuleBuilder ruleBuilder = (RuleBuilder) new RuleBuilder().allow().operation().withAnyName().atAnyLevel().andRequireExplicitResponseAuthorization().andThen();

                for (String scope : _scopes) {
                    String[] scope_details = scope.split(("\\/"));
                    if (scope_details.length != 2) {
                        // wrong format.
                        logger.warn("Scope format is wrong. Must have '/'.: " + scope);
                        continue;
                    }

                    String scopeLevel = scope_details[0];
                    String[] resourceAccess = scope_details[1].split("\\.");
                    if (resourceAccess.length != 2) {
                        // wrong format.
                        logger.warn("Scope format is wrong. Must have '.' for resource type and access.: " + scope_details[1]);
                        continue;
                    }
                    String resourceType = resourceAccess[0];
                    String access = resourceAccess[1];
                    
                    String[] accessParams = resourceAccess[1].split("\\?");
                    if (accessParams.length > 1) {
                        access = accessParams[0];

                        // We are not yet supporting param=value part. So, we ignore this.
                        // https://www.hl7.org/fhir/smart-app-launch/scopes-and-launch-context.html#fhir-resource-scope-syntax
                    }

                    // Now we set up the policy in FHIR intercept.
                    if (access.contains("r") || access.contains("s") || "read".equals(access) || "*".equals(access)) {
                        if ("*".equals(resourceType)) {
                            ruleBuilder = (RuleBuilder) ruleBuilder.allow().read().allResources().withAnyId().andThen();
                        } else {
                            ruleBuilder = (RuleBuilder) ruleBuilder.allow().read().resourcesOfType(resourceType).withAnyId().andThen();
                        }    
                    } 
                    
                    if (access.contains("c") || "write".equals(access) || "*".equals(access)) {
                        if ("*".equals(resourceType)) {
                            ruleBuilder = (RuleBuilder) ruleBuilder.allow().create().allResources().withAnyId().andThen();
                        } else {
                            ruleBuilder = (RuleBuilder) ruleBuilder.allow().create().resourcesOfType(resourceType).withAnyId().andThen();
                        }    
                    } 
                    
                    if (access.contains("u") || "write".equals(access) || "*".equals(access)) {
                        if ("*".equals(resourceType)) {
                            ruleBuilder = (RuleBuilder) ruleBuilder.allow().write().allResources().withAnyId().andThen();
                        } else {
                            ruleBuilder = (RuleBuilder) ruleBuilder.allow().write().resourcesOfType(resourceType).withAnyId().andThen();
                        }    
                    } 
                    
                    if (access.contains("d") || "write".equals(access) || "*".equals(access)) {
                        if ("*".equals(resourceType)) {
                            ruleBuilder = (RuleBuilder) ruleBuilder.allow().delete().allResources().withAnyId().andThen();
                        } else {
                            ruleBuilder = (RuleBuilder) ruleBuilder.allow().delete().resourcesOfType(resourceType).withAnyId().andThen();
                        }    
                    }
                }

                List<IAuthRule> built = ruleBuilder.build();
                for (IAuthRule authRule : built) {
                    logger.info(authRule.getName());
                    logger.info(authRule.toString());
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
