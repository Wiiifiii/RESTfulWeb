package com.wefky.RESTfulWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wefky.RESTfulWeb.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Updated to return Optional<User>
    Optional<User> findByUsername(String username);
}
