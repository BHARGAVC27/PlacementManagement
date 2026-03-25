package com.placement.tracker.dto;

import com.placement.tracker.enums.AppStatus;
import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long studentId,
        Long jobPostId,
        String companyName,
        AppStatus currentStatus,
        LocalDateTime appliedDate,
        String remarks
) {
}