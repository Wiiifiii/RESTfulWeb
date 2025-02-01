package com.wefky.RESTfulWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        // This view should be present in src/main/resources/templates/access-denied.html
        return "access-denied";
    }
}
