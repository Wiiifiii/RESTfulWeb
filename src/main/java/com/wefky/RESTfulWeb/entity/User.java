package com.wefky.RESTfulWeb.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
 * Represents a User in the system.
 */
@Entity 
@Table(name = "users") 
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
@EqualsAndHashCode(onlyExplicitlyIncluded = true) 
@ToString(exclude = "roles") 
public class User {

    /**
     * Represents the unique identifier for the user entity.
     * This field is automatically generated using the IDENTITY strategy.
     */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @EqualsAndHashCode.Include 
    private Long id;
    /**
     * Represents the username of the user.
     * This field is mandatory, unique, and cannot be null.
     * It is mapped to the 'username' column in the database.
     */
    @Column(nullable = false, unique = true) 
    private String username;
    /**
     * Represents the password of the user.
     * This field is mandatory and cannot be null.
     * It is mapped to the 'password' column in the database.
     */
    @Column(nullable = false) 
    private String password;
    /**
     * Represents the email address of the user.
     * This field is mandatory, unique, and cannot be null.
     * It is mapped to the 'email' column in the database.
     */
    @Column(nullable = false) 
    private boolean enabled;
    /**
     * Represents the set of roles associated with the user.
     * This field is fetched eagerly.
     * It is mapped by the 'users' attribute in the Role entity.
     * ManyToMany relationship is used to represent the association between User and Role entities.
     */
    @ManyToMany(fetch = FetchType.EAGER) 
    @JoinTable(
        name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "role_id")) 
    private Set<Role> roles; 
}
