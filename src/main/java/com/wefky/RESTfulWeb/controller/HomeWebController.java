package com.wefky.RESTfulWeb.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeWebController {

    @GetMapping("/")
    public String homePage(HttpServletRequest request, Model model) {
        // Provide the current request URI
        model.addAttribute("currentUri", request.getRequestURI());
        // Show index.html
        return "index";
    }
}
