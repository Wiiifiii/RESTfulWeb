package com.wefky.RESTfulWeb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

/**
 * ThymeleafConfig is a configuration class that sets up Thymeleaf with Spring Security dialect.
 * This configuration allows Thymeleaf templates to use Spring Security features.
 */
@Configuration
public class ThymeleafConfig {
    
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}
