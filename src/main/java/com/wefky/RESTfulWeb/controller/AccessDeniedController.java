package com.wefky.RESTfulWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedController {

    /**
     * Handles requests to the /access-denied endpoint.
     * This method returns the name of the view that displays the access denied page.
     * The view should be present in src/main/resources/templates/access-denied.html.
     *
     * @return the name of the access denied view
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
