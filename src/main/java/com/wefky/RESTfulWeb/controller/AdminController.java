package com.wefky.RESTfulWeb.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AdminController is a Spring MVC controller that handles HTTP requests
 * for the "/admin" URL path. It is secured to be accessible only by users
 * with the "ROLE_ADMIN" authority.
 * 
 * Annotations:
 * - @Controller: Indicates that this class serves the role of a controller in the Spring MVC framework.
 * - @RequestMapping("/admin"): Maps HTTP requests to /admin to this controller.
 * - @Secured("ROLE_ADMIN"): Ensures that only users with the "ROLE_ADMIN" authority can access the methods in this controller.
 * - @GetMapping: Maps HTTP GET requests to the adminHome method.
 * 
 * Methods:
 * - adminHome: Handles GET requests to the /admin URL and returns the name of the view to be rendered, which is "admin".
 * 
 * @return The name of the view to be rendered, "admin".
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Secured("ROLE_ADMIN")
    @GetMapping
    public String adminHome() {
        return "admin";
    }
}
