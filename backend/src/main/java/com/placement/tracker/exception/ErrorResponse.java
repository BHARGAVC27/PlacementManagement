package com.placement.tracker.exception;

import java.time.LocalDateTime;

// Standard error response body returned by global exception handler.
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String path
) {
}
