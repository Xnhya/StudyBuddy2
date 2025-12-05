package com.studybuddy.exception;

import com.studybuddy.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.studybuddy.controller")
public class GlobalExceptionHandler {

    // Maneja nuestras excepciones personalizadas
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex, WebRequest request) {
        ApiResponse<Object> response = new ApiResponse<>(
            false, 
            ex.getMessage(), 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    // Maneja recursos no encontrados
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        ApiResponse<Object> response = new ApiResponse<>(
            false, 
            ex.getMessage(), 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    // Maneja errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = new ApiResponse<>(
            false, 
            "Error de validación", 
            errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    // Maneja acceso denegado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ApiResponse<Object> response = new ApiResponse<>(
            false, 
            "Acceso denegado. No tienes permisos para esta operación.", 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    
    // Maneja todas las demás excepciones
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex, WebRequest request) {
        // En producción, no muestres el stack trace completo
        String message = "Ocurrió un error inesperado";
        
        ApiResponse<Object> response = new ApiResponse<>(
            false, 
            message, 
            null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}