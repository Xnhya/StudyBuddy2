package com.studybuddy.exception;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " con ID " + id + " no encontrado", "NOT_FOUND");
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "NOT_FOUND");
    }
}