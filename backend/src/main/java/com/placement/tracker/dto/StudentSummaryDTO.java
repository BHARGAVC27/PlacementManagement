package com.placement.tracker.dto;

import java.math.BigDecimal;

// DTO carrying basic student info for the admin student database view.
public record StudentSummaryDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String usn,
        String branch,
        BigDecimal currentCgpa,
        BigDecimal tenthPercent,
        BigDecimal twelfthPercent,
        Integer activeBacklogs
) {
}
