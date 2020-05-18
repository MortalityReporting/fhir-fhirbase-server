package edu.gatech.chai.fhir.smart.model;

public class JwkSetEntry {
	private String appId;
	private String publicKey;
	private String kid;
	private String kty;
	private String jti;
	private Integer exp;
	
	private String jwkRaw;
	
	public JwkSetEntry() {};
	
	public String getAppId() {
		return this.appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getPublicKey() {
		return this.publicKey;
	}
	
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getKid() {
		return this.kid;
	}
	
	public void setKid(String kid) {
		this.kid = kid;
	}

	public String getKty() {
		return this.kty;
	}
	
	public void setKty(String kty) {
		this.kty = kty;
	}

	public String getJti() {
		return this.jti;
	}
	
	public void setJti(String jti) {
		this.jti = jti;
	}
	
	public Integer getExp() {
		return this.exp;
	}
	
	public void setExp(Integer exp) {
		this.exp = exp;
	}

	public String getJwkRaw() {
		return this.jwkRaw;
	}
	
	public void setJwkRaw(String jwkRaw) {
		this.jwkRaw = jwkRaw;
	}
}
