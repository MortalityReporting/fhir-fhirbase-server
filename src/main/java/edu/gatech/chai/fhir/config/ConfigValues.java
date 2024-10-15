package edu.gatech.chai.fhir.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigValues {

    private String serverVersion;
	private String serverType;
    private String authDomain;
    private String authAudience;

    public String getServerVersion() {
        return this.serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getServerType() {
        return this.serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getAuthDomain() {
        return this.authDomain;
    }

    public void setAuthDomain(String authDomain) {
        this.authDomain = authDomain;
    }


    public String getAuthAudience() {
        return this.authAudience;
    }

    public void setAuthAudience(String authAudience) {
        this.authAudience = authAudience;
    }
}
