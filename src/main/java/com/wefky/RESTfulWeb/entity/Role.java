package com.wefky.RESTfulWeb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * Represents a Role assigned to Users.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "users") // Prevent recursive calls
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Include only 'id' in hashCode and equals
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "ROLE_ADMIN", "ROLE_USER"

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}
