package com.placement.tracker.dto;

import java.math.BigDecimal;

// Response/request DTO carrying student profile data between API and service.
public record StudentProfileDTO(
        BigDecimal currentCgpa,
        BigDecimal tenthPercent,
        BigDecimal twelfthPercent,
        Integer activeBacklogs,
        String branch,
        String resumeUrl
) {
}
