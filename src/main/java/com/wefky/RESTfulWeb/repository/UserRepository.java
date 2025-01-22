package com.wefky.RESTfulWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wefky.RESTfulWeb.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // We'll need this to load a user by username for login
    User findByUsername(String username);
}
