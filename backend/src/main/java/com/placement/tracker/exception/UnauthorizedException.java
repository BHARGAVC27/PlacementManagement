package com.placement.tracker.exception;

// Thrown when a user tries to access an operation without required permissions.
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
