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
                // Public routes
                .requestMatchers("/login", "/register", "/saveUser", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                // ADMIN routes
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Allow POST requests to delete endpoints for Admins
                .requestMatchers(HttpMethod.POST, "/web/images/delete/**",
                        "/web/images/delete-permanent/**",
                        "/web/locations/delete-permanent/**",
                        "/web/measurements/delete-permanent/**").hasRole("ADMIN")

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
            // Redirect to a custom access-denied page
            response.sendRedirect("/access-denied");
        };
    }
}
