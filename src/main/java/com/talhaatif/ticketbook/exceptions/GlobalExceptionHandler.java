package com.talhaatif.ticketbook.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;

@ControllerAdvice // ✅ This makes it a global exception handler
public class GlobalExceptionHandler {

    // ✅ Handle Not Found Exceptions (e.g., Booking not found)
    @ExceptionHandler(ResourceMissingException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceMissingException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    // ✅ Handle Unauthorized Access (e.g., user tries to cancel someone else's booking)
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
    }

    // ✅ Handle Generic Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Something went wrong: " + e.getMessage()));
    }
}
