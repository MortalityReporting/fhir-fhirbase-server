package edu.gatech.chai.fhir.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigValues {

	@Value("${server.version}")
    private String serverVersion;

	@Value("${server.type}")
	private String serverType;

    @Value("${auth.domain}")
    private String authDomain;

    @Value("${auth.audience}")
    private String authAudience;

    public String getServerVersion() {
        return this.serverVersion;
    }

    public String getServerType() {
        return this.serverType;
    }

    public String getAuthDomain() {
        return this.authDomain;
    }

    public String getAuthAudience() {
        return this.authAudience;
    }
}
