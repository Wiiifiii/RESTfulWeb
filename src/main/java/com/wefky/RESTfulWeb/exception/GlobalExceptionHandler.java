package com.wefky.RESTfulWeb.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * GlobalExceptionHandler is a controller advice class that handles exceptions thrown by the application.
 * It provides centralized exception handling across all @RequestMapping methods.
 * 
 * This class uses @ControllerAdvice to allow global exception handling.
 * It contains two exception handler methods:
 * 
 * 1. handleAccessDeniedException: Handles AccessDeniedException separately.
 *    - Logs the error message.
 *    - Adds a flash attribute with an error message indicating lack of permission.
 *    - Redirects the user to the access-denied page.
 * 
 * 2. handleAllExceptions: A catch-all handler for any other exceptions.
 *    - Logs the unexpected error.
 *    - Adds a flash attribute with a generic error message.
 *    - Redirects the user to the home page.
 * 
 * Logger is used to log error messages for debugging purposes.
 * RedirectAttributes is used to pass flash attributes (temporary attributes) to the redirected page.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle access-denied exceptions separately
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex, RedirectAttributes redirectAttributes) {
        logger.error("Access denied: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "You do not have permission to access this page.");
        return "redirect:/access-denied";
    }

    // Catch-all handler for any other exceptions
    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, RedirectAttributes redirectAttributes) {
        logger.error("An unexpected error occurred", ex);
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again.");
        return "redirect:/";
    }
}
