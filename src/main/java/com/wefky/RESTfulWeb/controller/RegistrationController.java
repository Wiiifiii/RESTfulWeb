package com.wefky.RESTfulWeb.controller;

import com.wefky.RESTfulWeb.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/saveUser")
    public String saveUser(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(defaultValue = "ROLE_USER") String role,
                           RedirectAttributes redirectAttributes) {

        // Convert role String to Set<String>
        Set<String> roles = Set.of(role);

        boolean registered = userService.registerUser(username, password, roles);

        if (!registered) {
            redirectAttributes.addFlashAttribute("error", "User already exists.");
            return "redirect:/register?error=UserExists";
        }

        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login?registered";
    }
}
