# fhir-fhirbase-server
This is a server module that configures and runs FHIR server. FHIR resource providers are instantiated in this server module. 

Authorization (SMART on FHIR), database connector driver, FHIR API routes, and other configurations are all implemented in here. 

This is a war file that can be executed within a Java application server such as Tomcat. 
## Installation
fhir-fhirbase jar file is required. Thus, this jar must first compiled and installed in the local maven library. 

Once fhir-fhirbase is installed, the following command can be executed in the fhir-fhirbase-server folder,

```
mvn clean install
```

This command will create war file in the target/ folder, which can be deployed in the java application server. 

If the server needs to be running in the local environment for such as testing, then the followin command will run this server using a Jetty server.

```
mvn jetty:run
```

We also provide all-in-one repository for this fhirbase based FHIR server. There, you can find more step-by-step instruction on how to deploy stable versiono of FHIR server. 
