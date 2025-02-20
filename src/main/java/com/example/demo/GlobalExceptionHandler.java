package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<?> handleFileValidation(FileValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMultipartError(MultipartException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "File upload error: " + e.getMessage()));
    }
}