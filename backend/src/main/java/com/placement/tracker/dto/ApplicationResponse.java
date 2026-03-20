package com.placement.tracker.dto;

import com.placement.tracker.enums.AppStatus;
import java.time.LocalDateTime;

// Response DTO representing application summary shown to student/admin.
public record ApplicationResponse(
        Long id,
        Long studentId,
        Long jobPostId,
        String companyName,
        AppStatus currentStatus,
        LocalDateTime appliedDate
) {
}