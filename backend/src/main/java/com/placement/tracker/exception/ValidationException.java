package com.placement.tracker.exception;

// Thrown when request data is invalid for business rules.
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
