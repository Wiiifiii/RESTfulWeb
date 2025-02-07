package com.wefky.RESTfulWeb.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle AccessDeniedException (403) using a custom access-denied page
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex) {
        logger.error("Access denied: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("access-denied"); // renders templates/access-denied.html
        mav.setStatus(HttpStatus.FORBIDDEN);
        mav.addObject("errorMessage", "You do not have permission to access this page.");
        return mav;
    }

    // Handle 404 errors (Not Found)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNotFoundException(NoHandlerFoundException ex) {
        logger.error("Page not found: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error/404"); // renders templates/error/404.html
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("errorMessage", "The page you are looking for was not found.");
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
