package com.placement.tracker.dto;

import java.math.BigDecimal;

// Response DTO used when listing students eligible for a specific job.
public record EligibleStudentResponse(
        Long studentId,
        String usn,
        String firstName,
        String lastName,
        String branch,
        BigDecimal currentCgpa,
        Integer activeBacklogs
) {
}
