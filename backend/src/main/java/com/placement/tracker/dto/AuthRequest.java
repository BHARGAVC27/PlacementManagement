package com.placement.tracker.dto;

// Request DTO for login/register input coming from client.
public record AuthRequest(
        String email,
        String password
) {
}
