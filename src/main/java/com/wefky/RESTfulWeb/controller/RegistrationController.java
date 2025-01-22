package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.User;
import com.wefky.RESTfulWeb.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository,
                                  BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register"; // register.html
    }

    @PostMapping("/saveUser")
    public String saveUser(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(defaultValue = "ROLE_USER") String role) {
        // Check if user already exists
        if (userRepository.findByUsername(username) != null) {
            // In a real app, show an error message
            return "redirect:/register?error=UserExists";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // Hash the password
        newUser.setRole(role);
        newUser.setEnabled(true);

        userRepository.save(newUser);
        return "redirect:/login?registered"; // redirect to login page
    }
}
