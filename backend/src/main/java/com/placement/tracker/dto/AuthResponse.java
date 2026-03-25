package com.placement.tracker.dto;

import com.placement.tracker.enums.Role;

// Response DTO returned after authentication with token and user details.
public record AuthResponse(
        String token,
        Role role,
        String email
) {
}
