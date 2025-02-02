package com.wefky.RESTfulWeb.config;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wefky.RESTfulWeb.entity.Role;
import com.wefky.RESTfulWeb.repository.RoleRepository;
import com.wefky.RESTfulWeb.repository.UserRepository;
import com.wefky.RESTfulWeb.service.UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initRolesAndAdmin() {
        return args -> {
            // Load existing roles from the database
            List<Role> existingRoles = roleRepository.findAll();

            // Check for ROLE_USER
            Role userRole;
            if (existingRoles.stream().noneMatch(r -> "ROLE_USER".equalsIgnoreCase(r.getName()))) {
                logger.info("ROLE_USER not found, creating ROLE_USER.");
                userRole = roleRepository.save(Role.builder().name("ROLE_USER").build());
            } else {
                userRole = existingRoles.stream()
                        .filter(r -> "ROLE_USER".equalsIgnoreCase(r.getName()))
                        .findFirst().get();
                logger.info("Found existing ROLE_USER.");
            }
            
            // Check for ROLE_ADMIN
            Role adminRole;
            if (existingRoles.stream().noneMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()))) {
                logger.info("ROLE_ADMIN not found, creating ROLE_ADMIN.");
                adminRole = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
            } else {
                adminRole = existingRoles.stream()
                        .filter(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()))
                        .findFirst().get();
                logger.info("Found existing ROLE_ADMIN.");
            }
            
            // Log current roles in the database
            logger.info("Current roles in the database:");
            roleRepository.findAll().forEach(role ->
                    logger.info("Role: {}", role.getName())
            );
            
            // Create admin user if not present, assign ROLE_ADMIN
            if (userRepository.findByUsername("admin").isEmpty()) {
                boolean created = userService.registerUser("admin", "admin", Set.of("ROLE_ADMIN"));
                if (created) {
                    logger.info("Admin user created with username 'admin' and password 'admin'.");
                }
            } else {
                logger.info("Admin user already exists.");
            }
            
            // For each non-admin user with no roles assigned, assign ROLE_USER.
            userRepository.findAll().forEach(user -> {
                if ((user.getRoles() == null || user.getRoles().isEmpty()) 
                        && !user.getUsername().equalsIgnoreCase("admin")) {
                    user.setRoles(Set.of(userRole));
                    userRepository.save(user);
                    logger.info("Assigned ROLE_USER to user '{}'.", user.getUsername());
                }
            });
            
            // Log all users loaded from the database
            logger.info("Current users in the database:");
            userRepository.findAll().forEach(user ->
                    logger.info("User: {} with roles: {}", user.getUsername(), user.getRoles())
            );
        };
    }
}
