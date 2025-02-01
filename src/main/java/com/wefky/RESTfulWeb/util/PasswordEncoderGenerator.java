package com.wefky.RESTfulWeb.util;

// Import the BCryptPasswordEncoder class from Spring Security
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderGenerator {
    public static void main(String[] args) {
        // The plain text password that needs to be hashed
        String password = "admin";
        
        // Create an instance of BCryptPasswordEncoder
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Hash the plain text password using BCrypt
        String hashedPassword = passwordEncoder.encode(password);
        
        // Print the hashed password to the console
        System.out.println("BCrypt Hashed Password: " + hashedPassword);
    }
}
