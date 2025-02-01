package com.wefky.RESTfulWeb.config;

import com.wefky.RESTfulWeb.entity.Role;
import com.wefky.RESTfulWeb.repository.RoleRepository;
import com.wefky.RESTfulWeb.repository.UserRepository;
import com.wefky.RESTfulWeb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Initializes roles and admin user on application startup.
 */
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
            // Ensure roles exist
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        logger.info("Creating ROLE_ADMIN");
                        return roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
                    });
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        logger.info("Creating ROLE_USER");
                        return roleRepository.save(Role.builder().name("ROLE_USER").build());
                    });

            // Create admin user if not present
            if (userRepository.findByUsername("admin").isEmpty()) {
                boolean created = userService.registerUser("admin", "admin", Set.of("ROLE_ADMIN"));
                if (created) {
                    logger.info("Admin user created with username 'admin' and password 'admin'.");
                }
            }

            // For each user in the system, if they have no roles assigned and are not the admin, assign ROLE_USER.
            userRepository.findAll().forEach(user -> {
                if ((user.getRoles() == null || user.getRoles().isEmpty()) 
                        && !user.getUsername().equalsIgnoreCase("admin")) {
                    user.setRoles(Set.of(userRole));
                    userRepository.save(user);
                    logger.info("Assigned ROLE_USER to user '{}'.", user.getUsername());
                }
            });
        };
    }
}
