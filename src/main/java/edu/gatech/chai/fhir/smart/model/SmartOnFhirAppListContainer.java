package edu.gatech.chai.fhir.smart.model;

import java.util.List;

public class SmartOnFhirAppListContainer {
	private List<SmartOnFhirAppEntry> appEntries;
	
	public List<SmartOnFhirAppEntry> getAppEntries() {
		return appEntries;
	}
	
	public void setAppEntries(List<SmartOnFhirAppEntry> appEntries) {
		this.appEntries = appEntries;
	}
}
