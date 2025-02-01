package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * Represents a User in the system.
 */
@Entity // Specifies that the class is an entity and is mapped to a database table.
@Table(name = "users") // Specifies the name of the database table to be used for mapping.
@Getter // Lombok annotation to generate getter methods for all fields.
@Setter // Lombok annotation to generate setter methods for all fields.
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor.
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields as parameters.
@Builder // Lombok annotation to implement the builder pattern for the class.
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Lombok annotation to generate equals and hashCode methods, including only explicitly specified fields.
@ToString(exclude = "roles") // Lombok annotation to generate a toString method, excluding the 'roles' field to prevent recursive calls.
public class User {

    @Id // Specifies the primary key of an entity.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Provides the specification of generation strategies for the primary keys.
    @EqualsAndHashCode.Include // Include only 'id' in the generated equals and hashCode methods.
    private Long id;

    @Column(nullable = false, unique = true) // Specifies the mapped column for a persistent property or field, with constraints.
    private String username;

    @Column(nullable = false) // Specifies the mapped column for a persistent property or field, with constraints.
    private String password;

    @Column(nullable = false) // Specifies the mapped column for a persistent property or field, with constraints.
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER) // Defines a many-to-many relationship with another entity, with eager fetching.
    @JoinTable(
        name = "user_roles", // Specifies the name of the join table.
        joinColumns = @JoinColumn(name = "user_id"), // Specifies the join column for this entity.
        inverseJoinColumns = @JoinColumn(name = "role_id")) // Specifies the join column for the other entity.
    private Set<Role> roles; // A set of roles associated with the user.
}
