package com.wefky.RESTfulWeb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, RedirectAttributes redirectAttributes) {
        logger.error("An unexpected error occurred", ex);
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again.");
        return "redirect:/";
    }

    // Add more specific exception handlers if needed
}
