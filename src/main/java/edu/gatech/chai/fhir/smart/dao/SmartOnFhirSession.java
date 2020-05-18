package edu.gatech.chai.fhir.smart.dao;

import java.util.List;

import edu.gatech.chai.fhir.smart.model.SmartOnFhirSessionEntry;

public interface SmartOnFhirSession {
	public int save(SmartOnFhirSessionEntry sessionEntry);
	public void update(SmartOnFhirSessionEntry sessionEntry);
	public void delete(String sessionId);
	public List<SmartOnFhirSessionEntry> get();
	public SmartOnFhirSessionEntry getSmartOnFhirSession(String sessionId);
}
