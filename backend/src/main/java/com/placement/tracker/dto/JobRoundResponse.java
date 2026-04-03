package com.placement.tracker.dto;

import java.time.LocalDateTime;

// Response DTO returned when reading round details.
public record JobRoundResponse(
        Long id,
        String roundName,
        LocalDateTime scheduledTime,
        String venueOrLink,
        String instructions,
        Long jobPostId,
        String companyName
) {
}
