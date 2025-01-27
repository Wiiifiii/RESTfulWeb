package com.wefky.RESTfulWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeWebController {

    @GetMapping("/")
    public String homePage() {
        // Return the name of the Thymeleaf template without .html
        return "index";
    }
}
