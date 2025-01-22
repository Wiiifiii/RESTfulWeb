package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // Will create a 'users' table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // Getters and setters
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)  // Ensure usernames are unique
    private String username;

    private String password; // Will be stored in hashed form

    private String role;     // e.g., "ROLE_USER" or "ROLE_ADMIN"

    private boolean enabled = true; // If you want to enable/disable accounts

}
