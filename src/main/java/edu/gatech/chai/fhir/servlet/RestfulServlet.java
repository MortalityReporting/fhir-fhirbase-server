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
package edu.gatech.chai.fhir.servlet;

import java.util.*;

import edu.gatech.chai.fhir.security.OIDCInterceptor;
import edu.gatech.chai.fhironfhirbase.provider.*;
import edu.gatech.chai.r4.security.SMARTonFHIRConformanceStatement;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.cors.CorsConfiguration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.IServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

/**
 * This servlet is the actual FHIR server itself
 */
public class RestfulServlet extends RestfulServer {

	private static final long serialVersionUID = 1L;
	private static FhirContext myFhirCtx = FhirContext.forR4();
	private WebApplicationContext myAppCtx;

	/**
	 * Constructor
	 */
	public RestfulServlet() {
		super(myFhirCtx);
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
	}

	/**
	 * This method is called automatically when the servlet is initializing.
	 */
	@Override
	public void initialize() {
		// Set server name
		setServerName("Raven FHIR Server (R4)");

		// If we have system environment variable to hardcode the base URL, do it now.
		String serverBaseUrl = System.getenv("SERVERBASE_URL");
		if (serverBaseUrl != null && !serverBaseUrl.isEmpty() && !serverBaseUrl.trim().equalsIgnoreCase("")) {
			serverBaseUrl = serverBaseUrl.trim();
			if (!serverBaseUrl.startsWith("http://") && !serverBaseUrl.startsWith("https://")) {
				serverBaseUrl = "https://" + serverBaseUrl;
			}

			if (serverBaseUrl.endsWith("/")) {
				serverBaseUrl = serverBaseUrl.substring(0, serverBaseUrl.length() - 1);
			}

			IServerAddressStrategy serverAddressStrategy = new HardcodedServerAddressStrategy(serverBaseUrl);
			setServerAddressStrategy(serverAddressStrategy);
		}

		/*
		 * Set non resource provider.
		 */
		List<Object> plainProviders = new ArrayList<Object>();
		SystemTransactionProvider systemTransactionProvider = new SystemTransactionProvider(myFhirCtx);
		ServerOperations serverOperations = new ServerOperations(myFhirCtx);

		/*
		 * Define resource providers
		 */
		List<IResourceProvider> providers = new ArrayList<IResourceProvider>();

		ConditionResourceProvider conditionResourceProvider = myAppCtx.getBean(ConditionResourceProvider.class, myFhirCtx);
		providers.add(conditionResourceProvider);

		EncounterResourceProvider encounterResourceProvider = myAppCtx.getBean(EncounterResourceProvider.class, myFhirCtx);
		providers.add(encounterResourceProvider);

		MedicationResourceProvider medicationResourceProvider = myAppCtx.getBean(MedicationResourceProvider.class, myFhirCtx);
		providers.add(medicationResourceProvider);

		MedicationStatementResourceProvider medicationStatementResourceProvider = myAppCtx.getBean(MedicationStatementResourceProvider.class, myFhirCtx);
		providers.add(medicationStatementResourceProvider);

		MedicationRequestResourceProvider medicationRequestResourceProvider = myAppCtx.getBean(MedicationRequestResourceProvider.class, myFhirCtx);
		providers.add(medicationRequestResourceProvider);

		ObservationResourceProvider observationResourceProvider = myAppCtx.getBean(ObservationResourceProvider.class, myFhirCtx);
		providers.add(observationResourceProvider);

		OrganizationResourceProvider organizationResourceProvider = myAppCtx.getBean(OrganizationResourceProvider.class, myFhirCtx);
		providers.add(organizationResourceProvider);

		PatientResourceProvider patientResourceProvider = myAppCtx.getBean(PatientResourceProvider.class, myFhirCtx);
		providers.add(patientResourceProvider);

		PractitionerResourceProvider practitionerResourceProvider = myAppCtx.getBean(PractitionerResourceProvider.class, myFhirCtx);
		providers.add(practitionerResourceProvider);

		PractitionerRoleResourceProvider practitionerRoleResourceProvider = myAppCtx.getBean(PractitionerRoleResourceProvider.class, myFhirCtx);
		providers.add(practitionerRoleResourceProvider);

		ProcedureResourceProvider procedureResourceProvider = myAppCtx.getBean(ProcedureResourceProvider.class, myFhirCtx);
		providers.add(procedureResourceProvider);

		DeviceResourceProvider deviceResourceProvider = myAppCtx.getBean(DeviceResourceProvider.class, myFhirCtx);
		providers.add(deviceResourceProvider);

		DeviceUseStatementResourceProvider deviceUseStatementResourceProvider = myAppCtx.getBean(DeviceUseStatementResourceProvider.class, myFhirCtx);
		providers.add(deviceUseStatementResourceProvider);

		DocumentReferenceResourceProvider documentReferenceResourceProvider = myAppCtx.getBean(DocumentReferenceResourceProvider.class, myFhirCtx);
		providers.add(documentReferenceResourceProvider);

		ConceptMapResourceProvider conceptMapResourceProvider = myAppCtx.getBean(ConceptMapResourceProvider.class, myFhirCtx);
		providers.add(conceptMapResourceProvider);

		ListResourceProvider listResourceProvider = myAppCtx.getBean(ListResourceProvider.class, myFhirCtx);
		providers.add(listResourceProvider);
		
		LocationResourceProvider locationResourceProvider = myAppCtx.getBean(LocationResourceProvider.class, myFhirCtx);
		providers.add(locationResourceProvider);

		RelatedPersonResourceProvider relatedPersonResourceProvider = myAppCtx.getBean(RelatedPersonResourceProvider.class, myFhirCtx);
		providers.add(relatedPersonResourceProvider);

		CompositionResourceProvider compositionResourceProvider = myAppCtx.getBean(CompositionResourceProvider.class, myFhirCtx);
		providers.add(compositionResourceProvider);

		MessageHeaderResourceProvider messageHeaderResourceProvider = myAppCtx.getBean(MessageHeaderResourceProvider.class, myFhirCtx);
		providers.add(messageHeaderResourceProvider);

		ParametersResourceProvider parametersResourceProvider = myAppCtx.getBean(ParametersResourceProvider.class, myFhirCtx);
		providers.add(parametersResourceProvider);

		setResourceProviders(providers);

		/*
		 * add system transaction provider to the plain provider.
		 */
		plainProviders.add(systemTransactionProvider);
		plainProviders.add(serverOperations);

//		setPlainProviders(plainProviders);
		registerProviders(plainProviders);
		/*
		 * Set conformance provider
		 */
		String authServerUrl = System.getenv("SMART_AUTHSERVERURL");
		String tokenServerUrl = System.getenv("SMART_TOKENSERVERURL");

		// CapabilityStatement must be loaded after providers.
		SMARTonFHIRConformanceStatement capbilityProvider = new SMARTonFHIRConformanceStatement(this);
		capbilityProvider.setPublisher("Georgia Tech - I3L");

		if (authServerUrl != null && !authServerUrl.isEmpty())
			capbilityProvider.setAuthServerUrl(authServerUrl);
		if (tokenServerUrl != null && !tokenServerUrl.isEmpty())
			capbilityProvider.setTokenServerUrl(tokenServerUrl);

		setServerConformanceProvider(capbilityProvider);

		/*
		 * Add page provider. Use memory based on for now.
		 */
		FifoMemoryPagingProvider pp = new FifoMemoryPagingProvider(5);
		pp.setDefaultPageSize(50);
		pp.setMaximumPageSize(100000);
		setPagingProvider(pp);

		/*
		 * Use a narrative generator. This is a completely optional step, but can be
		 * useful as it causes HAPI to generate narratives for resources which don't
		 * otherwise have one.
		 */
		INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
		getFhirContext().setNarrativeGenerator(narrativeGen);

		/*
		 * Enable CORS
		 */
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedHeader("x-fhir-starter");
		config.addAllowedHeader("Origin");
		config.addAllowedHeader("Accept");
		config.addAllowedHeader("X-Requested-With");
		config.addAllowedHeader("Content-Type");
		config.addAllowedHeader("Authorization");

		config.addAllowedOrigin("*");
		
		config.addExposedHeader("Location");
		config.addExposedHeader("Content-Location");
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

		CorsInterceptor corsInterceptor = new CorsInterceptor(config);
		registerInterceptor(corsInterceptor);

		/*
		 * This server interceptor causes the server to return nicely formatter and
		 * coloured responses instead of plain JSON/XML if the request is coming from a
		 * browser window. It is optional, but can be nice for testing.
		 */
		registerInterceptor(new ResponseHighlighterInterceptor());

		/*
		 * OpenID check interceptor to support SMART on FHIR
		 */

//		String url = System.getenv("SMART_INTROSPECTURL");
//		String authBasic = System.getenv("AUTH_BASIC");
//		String client_id = System.getenv("SMART_CLIENTID");
//		String client_secret = System.getenv("SMART_CLIENTSECRET");
//		String read_only = System.getenv("FHIR_READONLY");
////    	String local_bypass = System.getenv("LOCAL_BYPASS");
//
//		if (url == null)
//			url = getServletConfig().getInitParameter("introspectUrl");
//		if (authBasic == null)
//			authBasic = "None";
//		if (client_id == null)
//			client_id = getServletConfig().getInitParameter("clientId");
//		if (client_secret == null)
//			client_secret = getServletConfig().getInitParameter("clientSecret");
////    	if (local_bypass == null) 
////    		local_bypass = getServletConfig().getInitParameter("localByPass");
//		if (read_only == null)
//			read_only = getServletConfig().getInitParameter("readOnly");

		OIDCInterceptor oIDCInterceptor = new OIDCInterceptor();
//		oIDCInterceptor.setIntrospectUrl(url);
//		oIDCInterceptor.setAuthBasic(authBasic);
//		oIDCInterceptor.setClientId(client_id);
//		oIDCInterceptor.setClientSecret(client_secret);
////		oIDCInterceptor.setLocalByPass(local_bypass);
//		oIDCInterceptor.setReadOnly(read_only);

		registerInterceptor(oIDCInterceptor);

		/*
		 * Tells the server to return pretty-printed responses by default
		 */
		setDefaultPrettyPrint(true);

		/*
		 * Set response encoding.
		 */
		setDefaultResponseEncoding(EncodingEnum.JSON);

	}

}
