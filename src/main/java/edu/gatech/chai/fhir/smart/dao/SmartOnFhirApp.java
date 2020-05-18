package edu.gatech.chai.fhir.smart.dao;

import java.util.List;

import edu.gatech.chai.fhir.smart.model.SmartOnFhirAppEntry;

public interface SmartOnFhirApp {
	public int save(SmartOnFhirAppEntry appEntry);
	public void update(SmartOnFhirAppEntry appEntry);
	public void delete(String appId);
	public List<SmartOnFhirAppEntry> get();
	public SmartOnFhirAppEntry getSmartOnFhirApp(String appId);
}
