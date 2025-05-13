package edu.gatech.chai.fhir.config;

import org.springframework.stereotype.Component;

@Component
public class ConfigValues {

    private String serverVersion;
	private String serverType;
    private String serverUrl;
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

    public String getServerUrl() {
        return this.serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
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
