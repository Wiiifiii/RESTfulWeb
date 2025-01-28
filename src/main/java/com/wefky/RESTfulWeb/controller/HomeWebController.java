package com.wefky.RESTfulWeb.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeWebController {

    @GetMapping("/")
    public String homePage(HttpServletRequest request, Model model) {
        // Supply the current request URI as a model attribute
        String uri = request.getRequestURI(); // typically "/"
        model.addAttribute("currentUri", uri);
        return "index"; // index.html
    }
}
