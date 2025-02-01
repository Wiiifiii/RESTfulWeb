package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

/**
 * RegistrationController handles the registration process for new users.
 * It provides endpoints to show the registration form and to save a new user.
 */
@Controller
public class RegistrationController {

    private final UserService userService;

    /**
     * Constructor to initialize the RegistrationController with a UserService.
     *
     * @param userService the service to handle user-related operations
     */
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles GET requests to "/register" and returns the registration form view.
     *
     * @return the name of the registration form view
     */
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    /**
     * Handles POST requests to "/saveUser" to save a new user.
     * It takes the username, password, and role as parameters, registers the user,
     * and redirects to the appropriate view based on the registration result.
     *
     * @param username the username of the new user
     * @param password the password of the new user
     * @param role the role of the new user, defaults to "ROLE_USER"
     * @param redirectAttributes attributes for a redirect scenario
     * @return the redirect URL based on the registration result
     */
    @PostMapping("/saveUser")
    public String saveUser(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(defaultValue = "ROLE_USER") String role,
                           RedirectAttributes redirectAttributes) {

        // Convert role String to Set<String>
        Set<String> roles = Set.of(role);

        // Attempt to register the user
        boolean registered = userService.registerUser(username, password, roles);

        // Check if registration was successful
        if (!registered) {
            // If user already exists, add error message and redirect to registration form
            redirectAttributes.addFlashAttribute("error", "User already exists.");
            return "redirect:/register?error=UserExists";
        }

        // If registration is successful, add success message and redirect to login page
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login?registered";
    }
}
