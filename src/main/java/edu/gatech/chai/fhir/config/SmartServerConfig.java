package edu.gatech.chai.fhir.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import ca.uhn.fhir.context.ConfigurationException;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "edu.gatech.chai.omoponfhir.smart.servlet" })
public class SmartServerConfig implements WebMvcConfigurer {
	@Bean
	public ViewResolver viewResolver() {
		UrlBasedViewResolver bean = new UrlBasedViewResolver();

		bean.setViewClass(JstlView.class);
		bean.setPrefix("/WEB-INF/jsp/");
		bean.setSuffix(".jsp");

		return bean;
	}

	public static String getVersion(ResourceHandlerRegistry registry, String pkg, String name) {
		Properties props = new Properties();
		String resourceName = "/META-INF/maven/" + pkg + "/" + name + "/pom.properties";
		try {
			InputStream resourceAsStream = SmartServerConfig.class.getResourceAsStream(resourceName);
			if (resourceAsStream == null) {
				throw new ConfigurationException("Failed to load resource: " + resourceName);
			}
			props.load(resourceAsStream);
		} catch (IOException e) {
			throw new ConfigurationException("Failed to load resource: " + resourceName);
		}
		String version = props.getProperty("version");
		
		return version;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webjars/jquery/**").addResourceLocations("classpath:/META-INF/resources/webjars/jquery/"+SmartServerConfig.getVersion(registry, "org.webjars.bower", "jquery")+"/");
		registry.addResourceHandler("/webjars/jquery-ui/**").addResourceLocations("classpath:/META-INF/resources/webjars/jquery-ui/"+SmartServerConfig.getVersion(registry, "org.webjars", "jquery-ui")+"/");
		registry.addResourceHandler("/webjars/jquery-ui-themes/**").addResourceLocations("classpath:/META-INF/resources/webjars/jquery-ui-themes/"+SmartServerConfig.getVersion(registry, "org.webjars", "jquery-ui-themes")+"/");
		registry.addResourceHandler("/css/**").addResourceLocations("WEB-INF/css/");
	}
}
