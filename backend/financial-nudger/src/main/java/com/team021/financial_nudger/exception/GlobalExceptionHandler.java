package com.team021.financial_nudger.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handler for 404 errors (ResourceNotFoundException)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "NOT_FOUND");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Handler for 409 errors (ResourceAlreadyExistsException)
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleResourceConflict(ResourceAlreadyExistsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "CONFLICT");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Handler for 400 validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_FAILED");
        body.put("details", errors);
        return ResponseEntity.badRequest().body(body);
    }
    
    // Handler for file processing failures (e.g., PDF extraction or LLM issues)
    // We treat this as a 400 Bad Request, as the file provided couldn't be processed.
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleFileProcessingException(FileProcessingException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "FILE_PROCESSING_FAILED");
        body.put("message", ex.getMessage());
        // Log the exception details here for backend debugging (important!)
        // logger.error("File processing failed:", ex); 
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Handler for other 400 bad request errors (Keep this as a general catch-all)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = Map.of("error", "BAD_REQUEST", "message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    // Removed handler for GeminiClientException (class not present). If needed, add a concrete exception from LLM layer.
}