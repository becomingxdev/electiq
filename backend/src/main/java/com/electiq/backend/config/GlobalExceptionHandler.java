package com.electiq.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler to capture all unhandled exceptions across the application.
 * Ensures that the client always receives a consistent, user-friendly JSON response.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Catches all exceptions and returns a generic error message.
     * 
     * NOTE: Per user requirements, this returns HTTP 200 (OK) to the client 
     * to prevent frontend crashes or generic browser error pages, while 
     * logging the full stack trace internally for debugging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        // Log the actual error internally for troubleshooting
        logger.error("Unhandled exception caught by GlobalExceptionHandler: ", ex);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Something went wrong. Please try again.");
        
        // Return structured response with HTTP 200
        return ResponseEntity.ok(response);
    }
}
