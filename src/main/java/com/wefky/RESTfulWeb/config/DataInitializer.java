package com.wefky.RESTfulWeb.config;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wefky.RESTfulWeb.entity.Image;
import com.wefky.RESTfulWeb.entity.Location;
import com.wefky.RESTfulWeb.entity.Role;
import com.wefky.RESTfulWeb.repository.ImageRepository;
import com.wefky.RESTfulWeb.repository.LocationRepository;
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
    private final ImageRepository imageRepository;
    private final LocationRepository locationRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initRolesAdminAndLogData() {
        return args -> {
            // --- Initialize Roles ---
            // Check for ROLE_USER
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        logger.info("ROLE_USER not found. Creating ROLE_USER...");
                        return roleRepository.save(Role.builder().name("ROLE_USER").build());
                    });

            // Check for ROLE_ADMIN
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        logger.info("ROLE_ADMIN not found. Creating ROLE_ADMIN...");
                        return roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
                    });

            // --- Initialize Admin User ---
            if (userRepository.findByUsername("admin").isEmpty()) {
                boolean created = userService.registerUser("admin", "admin", Set.of("ROLE_ADMIN"));
                if (created) {
                    logger.info("Admin user created with username 'admin' and password 'admin'.");
                }
            } else {
                logger.info("Admin user already exists.");
            }

            // --- Assign ROLE_USER to non-admin users missing roles ---
            userRepository.findAll().forEach(user -> {
                if ((user.getRoles() == null || user.getRoles().isEmpty())
                        && !user.getUsername().equalsIgnoreCase("admin")) {
                    user.setRoles(Set.of(userRole));
                    userRepository.save(user);
                    logger.info("Assigned ROLE_USER to user '{}'.", user.getUsername());
                }
            });

            // --- Log Existing Image Data ---
            List<Image> images = imageRepository.findAll();
            logger.info("Found {} images in the database.", images.size());
            images.forEach(image -> {
                logger.info("Image ID: {}, Owner: {}, Title: {}, Content Type: {}",
                        image.getImageId(),
                        image.getOwner(),
                        image.getTitle(),
                        image.getContentType());
            });

            // --- Log Existing Location Data ---
            List<Location> locations = locationRepository.findAll();
            logger.info("Found {} locations in the database.", locations.size());
            locations.forEach(location -> {
                logger.info("Location ID: {}, City: {}, Postal Code: {}",
                        location.getLocationId(),
                        location.getCityName(),
                        location.getPostalCode());
            });
        };
    }
}
