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
package edu.gatech.chai.fhir.config;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.servlet.http.HttpServletRequest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.server.util.ITestingUiClientFactory;

public class MyAuthClientFactory implements ITestingUiClientFactory {

	@Override
	public IGenericClient newClient(FhirContext theFhirContext, HttpServletRequest theRequest,
			String theServerBaseUrl) {
		// Create a client
		IGenericClient client = theFhirContext.newRestfulGenericClient(theServerBaseUrl);

		String apiKey = theRequest.getParameter("apiKey");
		String authBasic = System.getenv("AUTH_BASIC");
		String authBearer = System.getenv("AUTH_BEARER");
		if (authBasic != null && !authBasic.isEmpty()) {
//			if (authType.startsWith("Basic ") || authType.startsWith("basic ")) {
			// Basic Auth
//				String basicAuth = authType.substring(6);
			String[] basicCredential = authBasic.split(":");
			if (basicCredential.length == 2) {
				// Bust have two parameters
				String username = basicCredential[0];
				String password = basicCredential[1];

				client.registerInterceptor(new BasicAuthInterceptor(username, password));
			}
		} else if (authBearer != null && (authBearer.startsWith("Bearer ") || authBearer.startsWith("bearer "))) {
			// Bearer API key. This overwrites the apiKey from theRequest parameter
			apiKey = authBearer.substring(7);
			if (isNotBlank(apiKey)) {
				client.registerInterceptor(new BearerTokenAuthInterceptor(apiKey));
			}
		}

//		theFhirContext.getRestfulClientFactory().setConnectionRequestTimeout(600000);
		theFhirContext.getRestfulClientFactory().setSocketTimeout(600000);

		return client;
	}

}
