package edu.gatech.chai.fhir.smart.servlet;

import edu.gatech.chai.fhir.smart.model.SmartOnFhirAppEntry;

public class SmartAuthRequest {
	private SmartOnFhirAppEntry appEntry;
	private String state;
	private String scope;
	private String launchContext;
	private String serverBaseUrl;
	
	
	public SmartAuthRequest() {}
	
	public SmartOnFhirAppEntry getAppEntry() {
		return this.appEntry;
	}
	
	public void setAppEntry(SmartOnFhirAppEntry appEntry) {
		this.appEntry = appEntry;
	}
	
	public String getState() {
		return this.state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public String getScope() {
		return this.scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	public String getLaunchContext() {
		return this.launchContext;
	}
	
	public void setLaunchContext(String launchContext) {
		this.launchContext = launchContext;
	}
	
	public String getServerBaseUrl() {
		return this.serverBaseUrl;
	}
	
	public void setServerBaseUrl(String serverBaseUrl) {
		this.serverBaseUrl = serverBaseUrl;
	}
}
