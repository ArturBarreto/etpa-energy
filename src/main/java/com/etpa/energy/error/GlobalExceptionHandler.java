package com.etpa.energy.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex, HttpServletRequest req){
        return build(HttpStatus.BAD_REQUEST, ex, req, List.of());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> notFound(NoSuchElementException ex, HttpServletRequest req){
        return build(HttpStatus.NOT_FOUND, ex, req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req){
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField()+": "+e.getDefaultMessage()).toList();
        return build(HttpStatus.BAD_REQUEST, ex, req, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> other(Exception ex, HttpServletRequest req){
        return build(HttpStatus.BAD_REQUEST, ex, req, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, Exception ex, HttpServletRequest req, List<String> details){
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(),
                ex.getMessage(), req.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
