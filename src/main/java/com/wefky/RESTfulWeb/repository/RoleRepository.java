package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RoleRepository is an interface that extends JpaRepository to provide CRUD operations
 * for the Role entity. It includes a method to find a Role by its name.
 * 
 * <p>This repository is annotated with @Repository, which indicates that it is a Spring 
 * Data repository and will be managed by the Spring container.</p>
 * 
 * @see JpaRepository
 * @see Role
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
