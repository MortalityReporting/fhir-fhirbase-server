package edu.gatech.chai.fhir.smart.servlet.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuth2Error {
	private String error;
	private String error_description;

	public OAuth2Error() {}
	
	public OAuth2Error(String error, String errorDescription) {
		this.error = error;
		this.error_description = errorDescription;
	}
	@JsonProperty("error")
	public String getError() {
		return this.error;
	}
	
	@JsonProperty("error")
	public void setError(String error) {
		this.error = error;
	}
	
	@JsonProperty("error_description")
	public String getErrorDescription() {
		return this.error_description;
	}
	
	@JsonProperty("error_description")
	public void setErrorDescription(String error_description) {
		this.error_description = error_description;
	}
	
	public String toString() {
		return "{"
				+ "\"error\": \"" + error + "\", "
				+ "\"error_description\": \"" + error_description + "\""
				+ "}";
	}
}
