package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.Role;
import com.wefky.RESTfulWeb.entity.User;
import com.wefky.RESTfulWeb.repository.RoleRepository;
import com.wefky.RESTfulWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing Users.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Registers a new user with the specified roles.
     *
     * @param username    the username
     * @param rawPassword the raw (plaintext) password
     * @param roleNames   the names of roles to assign (e.g., "ROLE_USER", "ROLE_ADMIN")
     * @return true if registration is successful, false otherwise
     */
    public boolean registerUser(String username, String rawPassword, Set<String> roleNames) {
        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("Registration failed: Username '{}' already exists.", username);
            return false;
        }

        // Fetch Role entities based on roleNames
        Set<Role> roles;
        try {
            roles = roleNames.stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            logger.error("Error assigning roles during registration: {}", e.getMessage());
            return false;
        }

        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .enabled(true)
                .build();

        userRepository.save(newUser);
        logger.info("User '{}' registered successfully with roles: {}", username, roleNames);
        return true;
    }
}
