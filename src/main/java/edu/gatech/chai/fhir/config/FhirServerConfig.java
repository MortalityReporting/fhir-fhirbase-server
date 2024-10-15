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

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import edu.gatech.chai.fhironfhirbase.database.DatabaseConfiguration;
import edu.gatech.chai.fhironfhirbase.database.DatabaseConfigurationImpl;

@Configuration
@EnableScheduling
@EnableTransactionManagement
@ComponentScans(value = { 
		@ComponentScan("edu.gatech.chai.fhironfhirbase.database"),
		@ComponentScan("edu.gatech.chai.fhironfhirbase.provider"),
		@ComponentScan("edu.gatech.chai.fhironfhirbase.operation"),
		@ComponentScan("edu.gatech.chai.fhir.config"),
		@ComponentScan("edu.gatech.chai.r4.security") })
@ImportResource({ "classpath:database-config.xml" })
public class FhirServerConfig {
	@Autowired
	DataSource dataSource;
//	@Bean(destroyMethod = "close")
//	public DataSource dataSource() {
//		BasicDataSource retVal = new BasicDataSource();
//		retVal.setDriver(new org.postgresql.Driver());
//		retVal.setUrl("jdbc:postgresql://localhost:5432/postgres?currentSchema=omop_v5");
//		retVal.setUsername("omop_v5");
//		retVal.setPassword("i3lworks");
//		return retVal;
//	}

	@Bean()
	public DatabaseConfiguration databaseConfiguration() {
		DatabaseConfigurationImpl databaseConfiguration = new DatabaseConfigurationImpl();

		// What driver do we want to use?
		String targetDatabase = System.getenv("TARGETDATABASE");
		databaseConfiguration.setSqlRenderTargetDialect(targetDatabase);

		databaseConfiguration.setDataSource(dataSource);
		if (targetDatabase == null || targetDatabase.isEmpty())
			databaseConfiguration.setSqlRenderTargetDialect("postgresql");

		return databaseConfiguration;
	}

	@Value("${server.version}")
    private String serverVersion;

	@Value("${server.type}")
	private String serverType;

    @Value("${auth.issuer-url}")
    private String authDomain;

    @Value("${auth.audience}")
    private String authAudience;

	@Bean()
	public ConfigValues configValues() {
		ConfigValues configValues = new ConfigValues();
		configValues.setServerVersion(this.serverVersion);
		configValues.setServerType(serverType);
		configValues.setAuthAudience(authAudience);
		configValues.setAuthDomain(authDomain);

		return configValues;
	}

//	@Bean()
//	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//		LocalContainerEntityManagerFactoryBean retVal = new LocalContainerEntityManagerFactoryBean();
//		retVal.setPersistenceUnitName("OMOPonFHIRv1");
////		retVal.setDataSource(dataSource());
//		retVal.setDataSource(dataSource);
//		retVal.setPackagesToScan("edu.gatech.chai.omopv5.model.entity");
//		retVal.setPersistenceProvider(new HibernatePersistenceProvider());
//		retVal.setJpaProperties(jpaProperties());
//		return retVal;
//	}
//
//	private Properties jpaProperties() {
//		Properties extraProperties = new Properties();
//		extraProperties.put("hibernate.dialect", org.hibernate.dialect.PostgreSQL94Dialect.class.getName());
////		extraProperties.put("hibernate.dialect", edu.gatech.chai.omopv5.jpa.enity.noomop.OmopPostgreSQLDialect.class.getName());
//		extraProperties.put("hibernate.format_sql", "true");
//		extraProperties.put("hibernate.show_sql", "false");
//		extraProperties.put("hibernate.hbm2ddl.auto", "update");
////		extraProperties.put("hibernate.hbm2ddl.auto", "none");
////		extraProperties.put("hibernate.enable_lazy_load_no_trans", "true");
//		extraProperties.put("hibernate.jdbc.batch_size", "20");
//		extraProperties.put("hibernate.cache.use_query_cache", "false");
//		extraProperties.put("hibernate.cache.use_second_level_cache", "false");
//		extraProperties.put("hibernate.cache.use_structured_entries", "false");
//		extraProperties.put("hibernate.cache.use_minimal_puts", "false");
//		// extraProperties.put("hibernate.search.model_mapping",
//		// SearchMappingFactory.class.getName());
//		extraProperties.put("hibernate.search.default.directory_provider", "filesystem");
//		extraProperties.put("hibernate.search.default.indexBase", "target/lucenefiles");
//		extraProperties.put("hibernate.search.lucene_version", "LUCENE_CURRENT");
//		// extraProperties.put("hibernate.search.default.worker.execution",
//		// "async");
//		return extraProperties;
//	}
//
//	@Bean()
//	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
//		JpaTransactionManager retVal = new JpaTransactionManager();
//		retVal.setEntityManagerFactory(entityManagerFactory);
//		return retVal;
//	}

}
