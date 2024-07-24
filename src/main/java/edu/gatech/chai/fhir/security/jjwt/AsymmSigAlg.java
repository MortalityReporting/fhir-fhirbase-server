package edu.gatech.chai.fhir.security.jjwt;

import java.security.Key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.ContextLoaderListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.gatech.chai.fhir.config.ConfigValues;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;

public class AsymmSigAlg extends LocatorAdapter<Key> {
    final static Logger logger = LoggerFactory.getLogger(AsymmSigAlg.class);

    private ConfigValues configValues;

	public AsymmSigAlg() {
		super();

        configValues = ContextLoaderListener.getCurrentWebApplicationContext().getBean(ConfigValues.class);
    }

    private Key lookupSignatureVerificationKey(String kid) {

        return null;
    }

	@Override
    public Key locate(JwsHeader header) {
        String keyId = header.getKeyId();
        String jwkSetEndPoint = "https://" + configValues.getAuthDomain() + "/.well-known/jwks.json";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(jwkSetEndPoint, String.class);
        if (HttpStatus.OK != response.getStatusCode()) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        
        JsonNode keys = root.path("keys");
        if (keys.isMissingNode()) {
            return null;
        }

        for (JsonNode key : keys) {
            if (keyId.equals(key.path("kid").asText())) {
                Jwk<?> pubKey = Jwks.parser().build().parse(key.toString());
                return pubKey.toKey();
            }
        }

        return null;
    }
}
