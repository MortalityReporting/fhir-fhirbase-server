package edu.gatech.chai.fhir.smart.Jwt;

import java.util.List;

import edu.gatech.chai.fhir.smart.model.JwkSetEntry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

public class JwtUtil {
	public static Jws<Claims> getJWTClaims(String jws) {
		Jws<Claims> claims = null;
		MySigningKeyResolver signingResolver = new MySigningKeyResolver();

		// JwtParser jwtParser = Jwts.builder().setSigningKeyResolver(signingResolver).build();
		// claims = jwtParser.parseClaimsJws(jws);
		return claims;
	}
	
	public static JwkSetEntry matchJwkSetEntry(List<JwkSetEntry> jwkSetEntries, String alg) {
		for (JwkSetEntry jwkSetEntry : jwkSetEntries) {
			String kty = jwkSetEntry.getKty();
			if (kty == null) continue;
			
			if (kty.regionMatches(0, alg, 0, 2)) { // RS or EC
				return jwkSetEntry;
			}
		}
		
		return null;
	}
}
