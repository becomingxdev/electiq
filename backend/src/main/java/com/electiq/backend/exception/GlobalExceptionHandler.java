package com.electiq.backend.exception;

import com.electiq.backend.config.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application-wide exception handler.
 *
 * <p>All exceptions bubble up from controllers and are caught here. Internal
 * details are never forwarded to the caller — only a safe, user-friendly
 * message is returned. The real error is always logged for internal observability.
 *
 * <h2>HTTP status note</h2>
 * <p>Per project requirements, validation errors return {@code 400 Bad Request}
 * for actionable client feedback, while all unexpected exceptions return
 * {@code 200 OK} with a generic error body to prevent frontend disruption.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles Bean Validation failures (e.g. blank question, oversized input).
     * Returns {@code 400} with a field-level message so the client can prompt
     * the user to correct their input.
     *
     * @param ex the validation exception from Spring's {@code @Valid} processing
     * @return structured error body with the first validation failure message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        String fieldError = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        logger.warn("Validation failed: {}", fieldError);
        return ResponseEntity.badRequest().body(Map.of("message", fieldError));
    }

    /**
     * Catches all other unhandled exceptions to prevent stack traces from leaking
     * to the client.
     *
     * <p>Returns {@code 500 Internal Server Error} with a generic message.
     * The full exception is logged at ERROR level for internal investigation.
     *
     * @param ex any unhandled runtime exception
     * @return generic, user-safe error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(Map.of("message", AppConstants.MSG_GENERIC_ERROR));
    }
}
