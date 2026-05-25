package com.psybergate.financialcalculator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(error(400, "Bad Request", message));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        return ResponseEntity.badRequest().body(error(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(TaxCalculationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTaxCalculationNotFound(TaxCalculationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(InvestmentForecastNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInvestmentForecastNotFound(InvestmentForecastNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(PropertyBondNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyBondNotFound(PropertyBondNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(CarLoanNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCarLoanNotFound(CarLoanNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(error(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(500, "Internal Server Error", ex.getMessage()));
    }

    private Map<String, Object> error(int status, String error, String message) {
        return Map.of("status", status, "error", error, "message", message,
                "timestamp", Instant.now().toString());
    }
}
