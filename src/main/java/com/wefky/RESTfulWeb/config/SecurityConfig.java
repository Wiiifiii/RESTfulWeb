package com.wefky.RESTfulWeb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.wefky.RESTfulWeb.service.MyUserDetailsService;

/**
 * Security configuration class for the RESTful web application.
 * 
 * Security Settings:
 * - Public routes: login, register, static resources, and access-denied page are accessible without authentication.
 * - ADMIN routes: restricted to users with the ADMIN role.
 * - POST requests for delete-permanent endpoints: restricted to Admins.
 * - POST requests for soft-delete endpoints: require authentication.
 * - API routes (including file retrieval via /api/images/{id}/file): require authentication.
 * - Web routes require authentication.
 * - Any other request requires authentication.
 *
 * Form Login:
 * - Uses a custom login page and redirects to /web/images upon successful login.
 *
 * Logout:
 * - Configures logout with a custom URL and success URL.
 *
 * CSRF:
 * - CSRF protection is disabled for API endpoints.
 *
 * Access Denied Handling:
 * - Redirects to a custom access-denied page.
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
                // Public routes including access-denied (for proper redirection)
                .requestMatchers("/login", "/register", "/saveUser", "/css/**", "/js/**", "/images/**", 
                                 "/locations/**", "/measurements/**", "/favicon.ico", "/access-denied").permitAll()

                // ADMIN routes
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Allow POST requests to delete-permanent endpoints for Admins
                .requestMatchers(HttpMethod.POST,
                        "/web/images/delete-permanent/**",
                        "/web/locations/delete-permanent/**",
                        "/web/measurements/delete-permanent/**").hasRole("ADMIN")

                // Allow POST requests to soft-delete endpoints for authenticated users
                .requestMatchers(HttpMethod.POST,
                        "/web/images/delete/**",
                        "/web/locations/delete/**",
                        "/web/measurements/delete/**").authenticated()

                // API routes (including the file retrieval endpoint) require authentication
                .requestMatchers("/api/**").authenticated()

                // Web routes require authentication
                .requestMatchers("/web/**").authenticated()

                // Any other request requires authentication
                .anyRequest().authenticated()
        );

        // Configure form login with custom login page and default success URL
        http.formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/web/images", true)
        );

        // Configure logout
        http.logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );

        // Handle access denied (403) by redirecting to a custom access-denied page
        http.exceptionHandling(exception -> exception
                .accessDeniedHandler(accessDeniedHandler())
        );

        // Disable CSRF protection for API endpoints (e.g., file retrieval)
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
        );

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect("/access-denied");
        };
    }
}
