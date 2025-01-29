package com.wefky.RESTfulWeb.config;

import com.wefky.RESTfulWeb.entity.Role;
import com.wefky.RESTfulWeb.entity.User;
import com.wefky.RESTfulWeb.repository.RoleRepository;
import com.wefky.RESTfulWeb.repository.UserRepository;
import com.wefky.RESTfulWeb.service.UserService;
import lombok.RequiredArgsConstructor;
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

    @Bean
    CommandLineRunner initRolesAndAdmin() {
        return args -> {
            // Initialize roles if they don't exist
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));
            // Add more roles if needed

            // Initialize admin user if not present
            if (!userRepository.findByUsername("admin").isPresent()) {
                boolean created = userService.registerUser("admin", "adminpassword", Set.of("ROLE_ADMIN"));
                if (created) {
                    System.out.println("Admin user created with username 'admin' and password 'adminpassword'.");
                }
            }

            // Assign ROLE_USER to existing users (e.g., user IDs 2 to 9)
            for (long userId = 2; userId <= 9; userId++) {
                userRepository.findById(userId).ifPresent(user -> {
                    if (user.getRoles() == null || user.getRoles().isEmpty()) {
                        user.setRoles(Set.of(userRole));
                        userRepository.save(user);
                        System.out.println("Assigned ROLE_USER to user '" + user.getUsername() + "'.");
                    }
                });
            }
        };
    }
}
