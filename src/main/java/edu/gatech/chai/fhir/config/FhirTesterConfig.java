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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.to.FhirTesterMvcConfig;
import ca.uhn.fhir.to.TesterConfig;
import ca.uhn.fhir.to.util.WebUtil;
import jakarta.annotation.Nonnull;

//@formatter:off
/**
 * This spring config file configures the web testing module. It serves two
 * purposes:
 * 1. It imports FhirTesterMvcConfig, which is the spring config for the
 *    tester itself
 * 2. It tells the tester which server(s) to talk to, via the testerConfig()
 *    method below
 */
@Configuration
// @Import(FhirTesterMvcConfig.class)
@EnableWebMvc
public class FhirTesterConfig extends FhirTesterMvcConfig {
	@Bean
	public TesterConfig testerConfig() {
		TesterConfig retVal = new TesterConfig();
		String serverBaseUrl = System.getenv("SERVERBASE_URL");
		if (serverBaseUrl == null || serverBaseUrl.isEmpty() || serverBaseUrl.trim().equalsIgnoreCase("")) {
			serverBaseUrl = "${serverBase}/fhir";
		} else {
			serverBaseUrl = serverBaseUrl.trim();
			if (!serverBaseUrl.startsWith("http://") && !serverBaseUrl.startsWith("https://")) {
				serverBaseUrl = "https://"+serverBaseUrl;
			}
			
			if (serverBaseUrl.endsWith("/")) {
				serverBaseUrl = serverBaseUrl.substring(0, serverBaseUrl.length()-1);
			}
		}
		retVal
			.addServer()
				.withId("home")
				.withFhirVersion(FhirVersionEnum.R4)
				.withBaseUrl(serverBaseUrl)
				.withName("Local Tester");
//			.addServer()
//				.withId("hapi")
//				.withFhirVersion(FhirVersionEnum.DSTU2)
//				.withBaseUrl("http://fhirtest.uhn.ca/baseDstu2")
//				.withName("Public HAPI Test Server");
		
		/*
		 * Use the method below to supply a client "factory" which can be used 
		 * if your server requires authentication
		 */
		MyAuthClientFactory clientFactory = new MyAuthClientFactory();
		retVal.setClientFactory(clientFactory);
		
		return retVal;
	}
	
	
}
//@formatter:on
