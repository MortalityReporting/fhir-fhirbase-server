package edu.gatech.chai.fhir.smart.Jwt;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.chai.fhir.smart.dao.JwkSetImpl;
import edu.gatech.chai.fhir.smart.model.JwkSetEntry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;

public class MySigningKeyResolver extends SigningKeyResolverAdapter {
	final static Logger logger = LoggerFactory.getLogger(MySigningKeyResolver.class);

	protected JwkSetImpl jwkSet;

	public MySigningKeyResolver() {
		super();

		jwkSet = new JwkSetImpl();
	}
	
	private Key getSigningKey(JwsHeader header, Claims claims) {

		String kid = header.getKeyId();
		String iss = claims.getIssuer();
		String alg = header.getAlgorithm();
		
		if (kid == null || iss == null || alg == null) {
			logger.error("kid = "+kid+", iss = "+iss+", alg = "+ alg +" cannot be null!.");
			return null;
		}
		// Get public key from JWKSet.
		String publicKeyStr = null;
		String kty = null;
		
		logger.debug("kid = "+kid+", iss = "+iss+", alg = "+ alg + "\n");
		List<JwkSetEntry> jwkSetEntries = jwkSet.getJwkSetByKidAndIss(kid, iss);
		JwkSetEntry jwkSetEntry = JwtUtil.matchJwkSetEntry(jwkSetEntries, alg);
		if (jwkSetEntry == null) return null;
				
		logger.debug(JwkSetImpl.printAppInfo(jwkSetEntry));
		
		publicKeyStr = jwkSetEntry.getPublicKey();
		logger.debug("public key string (before):\n"+publicKeyStr);

		publicKeyStr = publicKeyStr.replace("\n", "").replace("\r", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");

		logger.debug("public key string:\n"+publicKeyStr);

		kty = jwkSetEntry.getKty();
		
		byte[] decoded = Base64.getDecoder().decode(publicKeyStr);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
		if (kty == null || kty.isEmpty())
			kty = "RSA";

		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance(kty);
			if ("EC".equals(kty)) {
				return (ECPublicKey) kf.generatePublic(spec);
			} else {
				// assume it's RSA
				return (RSAPublicKey) kf.generatePublic(spec);
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Key resolveSigningKey(JwsHeader header, Claims claims) {
		// inspect the header or claims, lookup and return the signing key
		return getSigningKey(header, claims);
	}
}
