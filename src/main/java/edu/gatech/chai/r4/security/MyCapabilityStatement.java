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

import jakarta.servlet.http.HttpServletRequest;

import ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.OperationDefinition.OperationDefinitionParameterComponent;
import org.hl7.fhir.r4.model.OperationDefinition.OperationParameterUse;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.OperationDefinition;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.util.ExtensionConstants;
import edu.gatech.chai.fhir.config.ConfigValues;
import edu.gatech.chai.fhironfhirbase.utilities.ExtensionUtil;

/**
 * @author mc142local
 *
 */
public class MyCapabilityStatement extends ServerCapabilityStatementProvider {

	static String oauthURI = "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris";
	static String authorizeURI = "authorize";
	static String tokenURI = "token";
	static String registerURI = "register";

	String authorizeURIvalue = "http://localhost:8080/authorize";
	String tokenURIvalue = "http://localhost:8080/token";

	@Autowired
	private ConfigValues configValues;

	public MyCapabilityStatement(RestfulServer theRestfulServer) {
		super(theRestfulServer);

		WebApplicationContext context = ContextLoaderListener.getCurrentWebApplicationContext();
		configValues = context.getBean(ConfigValues.class);
	}

	@Override
	public CapabilityStatement getServerConformance(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
		CapabilityStatement cs = (CapabilityStatement) super.getServerConformance(theRequest, theRequestDetails);

		String title = "MDI FHIR Server";
		String name = null;
		String version = null;
		if (configValues.getServerVersion() != null && !configValues.getServerVersion().isBlank()) {
			version = configValues.getServerVersion().toLowerCase();
		} else {
			version = "version not available in properties";
		}

		if (configValues.getServerType() != null && !configValues.getServerType().isBlank()) {
			if ("EDRS".equalsIgnoreCase(configValues.getServerType())) {
				title = "Bluejay FHIR Server";
				name = "bluejay";
			} else {
				title = "Raven FHIR Server";
				name = "raven";
			}
		}

		cs.setTitle(title);
		cs.setName(name);
		cs.setVersion(version);
		cs
         .getSoftware()
		 .setName("MDI FHIR Server")
         .setVersion(version)
         .setReleaseDateElement(new DateTimeType("2025-08-13"));

		cs.setPublisher("Georgia Tech Research Institute - HEAT");

		Map<String, Long> counts = ExtensionUtil.getResourceCounts();

		for (CapabilityStatementRestComponent rest : cs.getRest()) {
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
			Coding coding = new Coding("http://terminology.hl7.org/CodeSystem/restful-security-service", "OAuth", "OAuth");
			codeableConcept.addCoding(coding);

			restSec.addService(codeableConcept);

			// We need to add SMART on FHIR required conformance statement.

			Extension secExtension = new Extension();
			secExtension.setUrl(oauthURI);

			// Extension authorizeExtension = new Extension();
			// authorizeExtension.setUrl(authorizeURI);
			// authorizeExtension.setValue(new UriType(authorizeURIvalue));

			Extension tokenExtension = new Extension();
			tokenExtension.setUrl(tokenURI);
			tokenExtension.setValue(new UriType(tokenURIvalue));

			// secExtension.addExtension(authorizeExtension);
			secExtension.addExtension(tokenExtension);

			restSec.addExtension(secExtension);

			rest.setSecurity(restSec);
		}

		return cs;
	}

	public void setAuthServerUrl(String url) {
		authorizeURIvalue = url;
	}

	public void setTokenServerUrl(String url) {
		tokenURIvalue = url;
	}

	private Extension myExtension(String url, Object value) {
		Extension extensionLabel = new Extension();

		extensionLabel.setUrl(url);
		if (value instanceof Type) {
			extensionLabel.setValue((Type) value);
		} else if (value instanceof Integer) {
			extensionLabel.setValue(new IntegerType((Integer) value));
		} else {
			extensionLabel.setValue(new StringType((String)value));
		}

		return extensionLabel;
	}

	@Override
	@Read(typeName = "OperationDefinition")
	public IBaseResource readOperationDefinition(@IdParam IIdType theId, RequestDetails theRequestDetails) {
		IBaseResource op = super.readOperationDefinition(theId, theRequestDetails);

		if (op instanceof OperationDefinition) {
			OperationDefinition od = (OperationDefinition) op;
			if ("Document".equalsIgnoreCase(od.getName())) {
				for (OperationDefinitionParameterComponent parameter : od.getParameter()) {
					if (Composition.SP_PATIENT.equals(parameter.getName())) {
						String nullString = null;
						parameter.setType(nullString);
						parameter.setMax("*");

						OperationDefinitionParameterComponent partParameter = new OperationDefinitionParameterComponent();
						partParameter.setName(Patient.SP_BIRTHDATE)
							.setUse(OperationParameterUse.IN)
							.setMin(0)
							.setMax("1")
							.setType("string")
							.setSearchType(SearchParamType.DATE)
							.addExtension(myExtension("urn:gtri:mapi-label", "Birthdate"))
							.addExtension(myExtension("urn:gtri:mapi-label-order", new IntegerType(3)));
						parameter.addPart(partParameter);

						partParameter = new OperationDefinitionParameterComponent();
						partParameter.setName(Patient.SP_FAMILY)
							.setUse(OperationParameterUse.IN)
							.setMin(0)
							.setMax("1")
							.setType("string")
							.setSearchType(SearchParamType.STRING)
							.addExtension(myExtension("urn:gtri:mapi-label", "Family Name"))
							.addExtension(myExtension("urn:gtri:mapi-label-order", new IntegerType(0)));
						parameter.addPart(partParameter);

						partParameter = new OperationDefinitionParameterComponent();
						partParameter.setName(Patient.SP_GIVEN)
							.setUse(OperationParameterUse.IN)
							.setMin(0)
							.setMax("1")
							.setType("string")
							.setSearchType(SearchParamType.STRING)
							.addExtension(myExtension("urn:gtri:mapi-label", "Given Name"))
							.addExtension(myExtension("urn:gtri:mapi-label-order", new IntegerType(1)));
						parameter.addPart(partParameter);

						partParameter = new OperationDefinitionParameterComponent();
						partParameter.setName(Patient.SP_GENDER)
							.setUse(OperationParameterUse.IN)
							.setMin(0)
							.setMax("1")
							.setType("string")
							.setSearchType(SearchParamType.TOKEN)
							.addExtension(myExtension("urn:gtri:mapi-label", "Gender"))
							.addExtension(myExtension("urn:gtri:mapi-label-order", new IntegerType(2)));
						parameter.addPart(partParameter);
					} else if ("tracking-number".equals(parameter.getName())) {
						parameter.addExtension(myExtension("urn:gtri:mapi-label", "Tracking Number"))
							.addExtension(myExtension("urn:gtri:mapi-label-order", new IntegerType(7)));
					} else if ("death-location".equals(parameter.getName())) {
						parameter.addExtension(myExtension("urn:gtri:mapi-label", "Death Location"))
							.addExtension(myExtension("urn:gtri:mapi-label-order", new IntegerType(4)));
					} else if ("death-date-pronounced".equals(parameter.getName())) {
						parameter.addExtension(myExtension("urn:gtri:mapi-label", "Pronounced Death Date"))
							.addExtension(myExtension("urn:gtri:mapi-label-order", new IntegerType(6)));
					} else if ("death-date".equals(parameter.getName())) {
						parameter.addExtension(myExtension("urn:gtri:mapi-label", "Death Date"))
							.addExtension(myExtension("urn:gtri:mapi-label-order", new IntegerType(5)));
					}
				}
			}
		}

		return op;
	}
}
