package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository is an interface that extends JpaRepository to provide CRUD operations 
 * for User entities. It includes a custom method to find a User by their username.
 * 
 * <p>This repository is annotated with @Repository, indicating that it's a Spring Data 
 * repository and will be automatically implemented by Spring at runtime.</p>
 * 
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see com.wefky.RESTfulWeb.model.User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
