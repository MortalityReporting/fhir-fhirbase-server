package edu.gatech.chai.fhir.smart.model;

import java.sql.Date;

public class SmartOnFhirSessionEntry {
	private String sessionId;
	private String state;
	private String appId;
	private String authorizationCode;
	private String accessToken;
	private Date authCodeExpirationDT;
	private Date accessTokenExpirationDT;
	private String refreshToken;

	public String getSessionId() {
		return this.sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public String getAppId() {
		return this.appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public String getAuthorizationCode() {
		return this.authorizationCode;
	}
	
	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}
	
	public String getAccessToken() {
		return this.accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public Date getAuthCodeExpirationDT() {
		return this.authCodeExpirationDT;
	}
	
	public void setAuthCodeExpirationDT(Date authCodeExpirationDT) {
		this.authCodeExpirationDT = authCodeExpirationDT;
	}

	public Date getAccessTokenExpirationDT() {
		return this.accessTokenExpirationDT;
	}
	
	public void setAccessTokenExpirationDT(Date accessTokenExpirationDT) {
		this.accessTokenExpirationDT = accessTokenExpirationDT;
	}
	
	public String getRefreshToken() {
		return this.refreshToken;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
