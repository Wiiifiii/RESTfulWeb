package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.User;
import com.wefky.RESTfulWeb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MyUserDetailsService is a service class that implements the UserDetailsService interface
 * to provide custom user authentication logic for Spring Security.
 * 
 * This service is annotated with @Service to indicate that it's a Spring-managed bean.
 * It uses a UserRepository to fetch user details from the database.
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor for MyUserDetailsService.
     * 
     * @param userRepository the UserRepository used to fetch user details from the database
     */
    @Autowired
    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the user details by username.
     * 
     * @param username the username of the user to be loaded
     * @return UserDetails containing user information and authorities
     * @throws UsernameNotFoundException if the user is not found in the database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch the user from the database using the username
        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Convert the user's roles to GrantedAuthority objects
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName())) // assuming role names include "ROLE_"
                .collect(Collectors.toSet());

        // Build and return a UserDetails object with the user's information and authorities
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
