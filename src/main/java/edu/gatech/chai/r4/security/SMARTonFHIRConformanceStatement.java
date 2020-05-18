/*******************************************************************************
 * Copyright (c) 2019 Georgia Tech Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package edu.gatech.chai.r4.security;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.util.ExtensionConstants;
import edu.gatech.chai.fhironfhirbase.utilities.ExtensionUtil;

/**
 * @author mc142local
 *
 */
public class SMARTonFHIRConformanceStatement extends ServerCapabilityStatementProvider {

	// static String authorizeURI =
	// "http://fhir-registry.smarthealthit.org/Profile/oauth-uris#authorize";
	// static String tokenURI =
	// "http://fhir-registry.smarthealthit.org/Profile/oauth-uris#token";
	// static String registerURI =
	// "http://fhir-registry.smarthealthit.org/Profile/oauth-uris#register";

	static String oauthURI = "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris";
	static String authorizeURI = "authorize";
	static String tokenURI = "token";
	static String registerURI = "register";

	String authorizeURIvalue = "http://localhost:8080/authorize";
	String tokenURIvalue = "http://localhost:8080/token";

	public SMARTonFHIRConformanceStatement(RestfulServer theRestfulServer) {
//		super(theRestfulServer);
//		setCache(false);

//		try {
//			InetAddress addr = java.net.InetAddress.getLocalHost();
//			System.out.println(addr);
//			String hostname = addr.getCanonicalHostName();
//			System.out.println("Hostname of system = " + hostname);
//
//			// authorizeURIvalue = "http://"+hostname+":9085/authorize";
//			// tokenURIvalue = "http://"+hostname+":9085/token";
//			// registerURIvalue = "http://"+hostname+":9085/register";
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	public CapabilityStatement getServerConformance(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
		CapabilityStatement conformanceStatement = super.getServerConformance(theRequest, theRequestDetails);

		Map<String, Long> counts = ExtensionUtil.getResourceCounts();

		for (CapabilityStatementRestComponent rest : conformanceStatement.getRest()) {
			for (CapabilityStatementRestResourceComponent nextResource : rest.getResource()) {
				Long count = counts.get(nextResource.getTypeElement().getValueAsString());
				if (count != null) {
					nextResource.addExtension(
							new Extension(ExtensionConstants.CONF_RESOURCE_COUNT, new DecimalType(count)));
				}
			}

			CapabilityStatementRestSecurityComponent restSec = new CapabilityStatementRestSecurityComponent();

			// Set security.service
			CodeableConcept codeableConcept = new CodeableConcept();
			Coding coding = new Coding("https://www.hl7.org/fhir/codesystem-restful-security-service.html",
					"SMART-on-FHIR", "SMART-on-FHIR");
			codeableConcept.addCoding(coding);

			restSec.addService(codeableConcept);

			// We need to add SMART on FHIR required conformance statement.

			Extension secExtension = new Extension();
			secExtension.setUrl(oauthURI);

			Extension authorizeExtension = new Extension();
			authorizeExtension.setUrl(authorizeURI);
			authorizeExtension.setValue(new UriType(authorizeURIvalue));

			Extension tokenExtension = new Extension();
			tokenExtension.setUrl(tokenURI);
			tokenExtension.setValue(new UriType(tokenURIvalue));

//		Extension registerExtension = new Extension();
//		registerExtension.setUrl(registerURI);
//		registerExtension.setValue(new UriType(registerURIvalue));

			secExtension.addExtension(authorizeExtension);
			secExtension.addExtension(tokenExtension);
//		secExtension.addExtension(registerExtension);

			restSec.addExtension(secExtension);

			// restSec.addUndeclaredExtension(authorizeExtension);
			// restSec.addUndeclaredExtension(tokenExtension);
			// restSec.addUndeclaredExtension(registerExtension);

			rest.setSecurity(restSec);

//		List<CapabilityStatementRestComponent> rests = conformanceStatement.getRest();
//		if (rests == null || rests.size() <= 0) {
//			CapabilityStatementRestComponent rest = new CapabilityStatementRestComponent();
//			rest.setSecurity(restSec);
//			conformanceStatement.addRest(rest);
//		} else {
//			CapabilityStatementRestComponent rest = rests.get(0);
//			rest.setSecurity(restSec);
//		}
		}
		return conformanceStatement;
	}

	public void setAuthServerUrl(String url) {
		authorizeURIvalue = url;
	}

	public void setTokenServerUrl(String url) {
		tokenURIvalue = url;
	}
}
