package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * Represents a Role assigned to Users.
 * This entity is mapped to the 'roles' table in the database.
 */
@Entity
@Table(name = "roles")
@Getter // Lombok annotation to generate getter methods for all fields
@Setter // Lombok annotation to generate setter methods for all fields
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields as arguments
@Builder // Lombok annotation to implement the builder pattern for this class
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Lombok annotation to generate equals and hashCode methods, including only explicitly included fields
@ToString(exclude = "users") // Lombok annotation to generate a toString method, excluding the 'users' field to prevent recursive calls
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Specifies that the 'id' field is the primary key and its value is automatically generated
    @EqualsAndHashCode.Include // Include only 'id' in the generated equals and hashCode methods
    private Long id; // Unique identifier for each role

    @Column(nullable = false, unique = true) // Specifies that the 'name' field is a column in the database, cannot be null, and must be unique
    private String name; // Name of the role, e.g., "ROLE_ADMIN", "ROLE_USER"

    @ManyToMany(mappedBy = "roles") // Specifies a many-to-many relationship with the User entity, with 'roles' being the owning side
    private Set<User> users; // Set of users that have this role
}
