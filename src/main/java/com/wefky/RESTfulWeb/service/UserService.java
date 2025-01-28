package com.wefky.RESTfulWeb.service;

import com.wefky.RESTfulWeb.entity.User;
import com.wefky.RESTfulWeb.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registerUser(String username, String rawPassword, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return false;
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setRoles(Set.of(role));
        newUser.setEnabled(true);
        userRepository.save(newUser);
        return true;
    }
}
