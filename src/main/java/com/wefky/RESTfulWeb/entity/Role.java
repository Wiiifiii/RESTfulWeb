package com.wefky.RESTfulWeb.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a Role assigned to Users.
 * This entity is mapped to the 'roles' table in the database.
 */
@Entity
@Table(name = "roles")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
@EqualsAndHashCode(onlyExplicitlyIncluded = true) 
@ToString(exclude = "users") 
public class Role {

    /**
     * Represents the unique identifier for the role entity.
     * This field is automatically generated using the IDENTITY strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @EqualsAndHashCode.Include 
    private Long id;
    /**
     * Represents the name of the role.
     * This field is mandatory, unique, and cannot be null.
     * It is mapped to the 'name' column in the database.
     */
    @Column(nullable = false, unique = true) 
    private String name; 
    /**
     * Represents the set of users associated with the role.
     * This field is mapped by the 'roles' attribute in the User entity.
     */
    @ManyToMany(mappedBy = "roles") 
    private Set<User> users; 
}
