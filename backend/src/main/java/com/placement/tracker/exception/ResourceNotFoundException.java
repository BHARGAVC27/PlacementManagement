package com.placement.tracker.exception;

// Thrown when a requested entity/resource does not exist.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
