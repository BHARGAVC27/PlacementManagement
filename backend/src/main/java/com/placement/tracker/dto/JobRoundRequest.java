package com.placement.tracker.dto;

import java.time.LocalDateTime;

// Request DTO used to create a new selection round for a job.
public record JobRoundRequest(
        String roundName,
        LocalDateTime scheduledTime,
        String venueOrLink,
        String instructions,
        Long jobPostId
) {
}
