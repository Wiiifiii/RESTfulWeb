package com.wefky.RESTfulWeb.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * GlobalExceptionHandler is a controller advice class that handles exceptions thrown by the application.
 * It provides centralized exception handling across all @RequestMapping methods.
 * 
 * This class contains three exception handler methods:
 * 
 * 1. handleAccessDeniedException: Handles AccessDeniedException separately.
 *    - Logs the error.
 *    - Returns the "access-denied" view (templates/access-denied.html) with HTTP status 403.
 * 
 * 2. handleNotFoundException: Handles 404 errors.
 *    - Logs the error.
 *    - Returns the "error/404" view (templates/error/404.html) with HTTP status 404.
 * 
 * 3. handleAllExceptions: A catch-all handler for any other exceptions.
 *    - Logs the error.
 *    - Returns the "error/500" view (templates/error/500.html) with HTTP status 500.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle access-denied exceptions separately
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex) {
        logger.error("Access denied: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("access-denied"); // renders templates/access-denied.html
        mav.setStatus(HttpStatus.FORBIDDEN);
        mav.addObject("errorMessage", "You do not have permission to access this page.");
        return mav;
    }

    // Handle 404 errors (if configured to throw NoHandlerFoundException)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNotFoundException(NoHandlerFoundException ex) {
        logger.error("Page not found: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error/404"); // renders templates/error/404.html
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("errorMessage", "The requested page was not found.");
        return mav;
    }

    // Catch-all handler for any other exceptions (500)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception ex) {
        logger.error("An unexpected error occurred", ex);
        ModelAndView mav = new ModelAndView("error/500"); // renders templates/error/500.html
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("errorMessage", "An unexpected error occurred. Please try again later.");
        return mav;
    }
}
