package com.wefky.RESTfulWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class for the RESTful Web Application.
 * This class is annotated with @SpringBootApplication, which is a convenience annotation that adds:
 * - @Configuration: Tags the class as a source of bean definitions for the application context.
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings.
 * - @ComponentScan: Tells Spring to look for other components, configurations, and services in the com/wefky/RESTfulWeb package, allowing it to find the controllers.
 *
 * The main() method uses Spring Boot's SpringApplication.run() method to launch the application.
 *
 * @param args Command line arguments passed to the application.
 */
@SpringBootApplication
public class ResTfulWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResTfulWebApplication.class, args);
	}

}
