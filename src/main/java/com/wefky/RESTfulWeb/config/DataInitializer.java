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
/**
 * DataInitializer is a configuration class that initializes roles and an admin user 
 * in the database upon application startup. It ensures that the necessary roles 
 * and users are present for the application to function correctly.
 * 
 * <p>This class uses the following components:
 * <ul>
 *   <li>{@link UserService} - Service for user-related operations</li>
 *   <li>{@link RoleRepository} - Repository for role-related database operations</li>
 *   <li>{@link UserRepository} - Repository for user-related database operations</li>
 * </ul>
 * 
 * <p>The initialization logic is executed by a {@link CommandLineRunner} bean, which:
 * <ul>
 *   <li>Ensures that the "ROLE_USER" role exists in the database, creating it if necessary</li>
 *   <li>Creates an admin user with the username "admin" and password "admin" if it does not already exist</li>
 *   <li>Assigns the "ROLE_USER" role to all users who do not have any roles assigned, except for the admin user</li>
 * </ul>
 * 
 * <p>Logging is performed using a {@link Logger} to provide information about the initialization process.
 * 
 * @see UserService
 * @see RoleRepository
 * @see UserRepository
 * @see CommandLineRunner
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    // Injecting required services and repositories
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    
    // Logger for logging information
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    /**
     * Bean that runs on application startup to initialize roles and admin user.
     * 
     * @return CommandLineRunner to execute the initialization logic
     */
    @Bean
    CommandLineRunner initRolesAndAdmin() {
        return args -> {
            // Ensure ROLE_USER exists in the database
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