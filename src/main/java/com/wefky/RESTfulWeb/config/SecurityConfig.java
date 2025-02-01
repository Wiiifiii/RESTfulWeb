package com.wefky.RESTfulWeb.config;

import com.wefky.RESTfulWeb.service.MyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration class for the RESTful web application.
 * This class configures authentication, authorization, and security settings.
 * 
 * Annotations:
 * - @Configuration: Indicates that this class is a configuration class.
 * - @EnableGlobalMethodSecurity: Enables method-level security with secured and pre/post annotations.
 * 
 * Beans:
 * - BCryptPasswordEncoder: Provides a password encoder bean using BCrypt hashing algorithm.
 * - AuthenticationProvider: Configures the authentication provider with user details service and password encoder.
 * - SecurityFilterChain: Configures the security filter chain with authentication, authorization, and other security settings.
 * - AccessDeniedHandler: Handles access denied exceptions by redirecting to a custom access-denied page.
 * 
 * Security Settings:
 * - Public routes: Allows access to login, register, static resources, and access-denied page without authentication.
 * - ADMIN routes: Restricts access to /admin/** endpoints to users with ADMIN role.
 * - POST requests for delete-permanent endpoints: Restricts access to users with ADMIN role.
 * - POST requests for delete (soft delete) endpoints: Requires authentication.
 * - API routes: Requires authentication for /api/** endpoints.
 * - Web routes: Requires authentication for /web/** endpoints.
 * - Any other request: Requires authentication.
 * 
 * Form Login:
 * - Configures form login with custom login page and default success URL.
 * 
 * Logout:
 * - Configures logout with custom logout URL and success URL.
 * 
 * Access Denied Handling:
 * - Redirects to custom access-denied page on access denied exceptions.
 * 
 * CSRF:
 * - Disables CSRF protection for API endpoints.
 * 
 * @param myUserDetailsService The user details service for loading user-specific data.
 */
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;

    public SecurityConfig(MyUserDetailsService myUserDetailsService) {
        this.myUserDetailsService = myUserDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(myUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        http.authorizeHttpRequests(auth -> auth
                // Public routes including /access-denied so the redirect can occur without a block
                .requestMatchers("/login", "/register", "/saveUser", "/css/**", "/js/**", "/images/**", "/locations/**", "/measurements/**", "/favicon.ico", "/access-denied").permitAll()

                // ADMIN routes
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Allow POST requests to delete-permanent endpoints for Admins
                .requestMatchers(HttpMethod.POST,
                        "/web/images/delete-permanent/**",
                        "/web/locations/delete-permanent/**",
                        "/web/measurements/delete-permanent/**").hasRole("ADMIN")
                
                // Allow POST requests to delete (soft delete) for authenticated users
                .requestMatchers(HttpMethod.POST,
                        "/web/images/delete/**",
                        "/web/locations/delete/**",
                        "/web/measurements/delete/**").authenticated()

                // API routes require authentication
                .requestMatchers("/api/**").authenticated()

                // Web routes require authentication
                .requestMatchers("/web/**").authenticated()

                // Any other request
                .anyRequest().authenticated()
        );

        // Configure form login
        http.formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/", true)
        );

        // Configure logout
        http.logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );

        // Handle access denied (403)
        http.exceptionHandling(exception -> exception
                .accessDeniedHandler(accessDeniedHandler())
        );

        // Disable CSRF for API endpoints
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
        );

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // Redirect to the custom access-denied page
            response.sendRedirect("/access-denied");
        };
    }
}
