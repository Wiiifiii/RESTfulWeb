/**
 * HomeWebController is a Spring MVC controller that handles HTTP GET requests
 * to the root URL ("/"). It serves the home page of the web application.
 * 
 * Annotations:
 * @Controller - Indicates that this class serves as a controller in a Spring MVC application.
 * 
 * Methods:
 * homePage(HttpServletRequest request, Model model) - Handles GET requests to the root URL ("/").
 * 
 * @param request - The HttpServletRequest object that contains the request the client made to the server.
 * @param model - The Model object that is used to pass attributes to the view.
 * @return A string representing the name of the view to be rendered, in this case, "index" which corresponds to index.html.
 */
package com.wefky.RESTfulWeb.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeWebController {

    @GetMapping("/")
    public String homePage(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
        return "index"; // index.html
    }
}
