package com.wefky.RESTfulWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The LoginController class handles HTTP GET requests for the login page.
 * It is annotated with @Controller to indicate that it is a Spring MVC controller.
 */
@Controller
public class LoginController {

    /**
     * Handles GET requests to the "/login" URL.
     * This method returns the name of the view that should be rendered for the login page.
     *
     * @return A string representing the name of the view to be rendered, in this case "login".
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}
