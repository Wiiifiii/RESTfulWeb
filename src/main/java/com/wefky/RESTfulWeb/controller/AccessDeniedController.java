package com.wefky.RESTfulWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied"; // Thymeleaf template for access denied page
    }
}