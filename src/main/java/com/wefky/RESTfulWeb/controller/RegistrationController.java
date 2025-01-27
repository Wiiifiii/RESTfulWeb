package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.entity.User;
import com.wefky.RESTfulWeb.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

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
                           @RequestParam(defaultValue = "ROLE_USER") String role,
                           RedirectAttributes redirectAttributes) {
        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            // Add flash attribute for error
            redirectAttributes.addFlashAttribute("error", "User already exists.");
            return "redirect:/register?error=UserExists";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // Hash the password
        newUser.setRoles(Set.of(role));
        newUser.setEnabled(true);

        userRepository.save(newUser);
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login?registered"; // redirect to login page
    }
}
