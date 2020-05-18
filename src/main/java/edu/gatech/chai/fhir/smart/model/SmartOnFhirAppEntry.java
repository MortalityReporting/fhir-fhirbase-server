package edu.gatech.chai.fhir.smart.model;

public class SmartOnFhirAppEntry {
	private String appId;  // client-id in SMART on FHIR
	private String appName;
	private String appType;
	private String redirectUri; 
	private String launchUri; // App URL
	private String scope;
	
	public SmartOnFhirAppEntry() {}
	
	public String getAppId() {
		return this.appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return this.appName;
	}
	
	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppType() {
		return this.appType;
	}
	
	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getRedirectUri() {
		return this.redirectUri;
	}
	
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getLaunchUri() {
		return this.launchUri;
	}
	
	public void setLaunchUri(String launchUri) {
		this.launchUri = launchUri;
	}

	public String getScope() {
		return this.scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
}
