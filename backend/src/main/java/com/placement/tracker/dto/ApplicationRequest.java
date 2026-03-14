package com.placement.tracker.dto;

// Request DTO for applying to a job; student is taken from JWT.
public record ApplicationRequest(
        Long jobPostId
) {
}
