package edu.gatech.chai.fhir.smart.dao;

import java.util.List;

import edu.gatech.chai.fhir.smart.model.JwkSetEntry;

public interface JwKSet {
	public int save(JwkSetEntry jwkSetEntry);
	public void update(JwkSetEntry jwkSetEntry);
	public void delete(String appId);
	public List<JwkSetEntry> get();
	public List<JwkSetEntry> getJwkSetByAppId(String appId);
	public List<JwkSetEntry> getJwkSetByKidAndIss(String kid, String iss);
}
