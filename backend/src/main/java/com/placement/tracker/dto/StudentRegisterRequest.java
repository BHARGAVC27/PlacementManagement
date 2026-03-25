package com.placement.tracker.dto;

import java.math.BigDecimal;

// Request DTO for full student registration with profile details.
public record StudentRegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String usn,
        String phone,
        String branch,
        BigDecimal currentCgpa,
        BigDecimal tenthPercent,
        BigDecimal twelfthPercent,
        Integer activeBacklogs,
        String resumeUrl
) {
}