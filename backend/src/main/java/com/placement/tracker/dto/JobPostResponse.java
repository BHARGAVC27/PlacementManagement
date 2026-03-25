package com.placement.tracker.dto;

import com.placement.tracker.enums.JobStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

// Response DTO returned to clients when reading job post data.
public record JobPostResponse(
        Long id,
        String companyName,
        String roleDescription,
        BigDecimal packageLPA,
        LocalDate deadline,
        JobStatus status,
        BigDecimal minCgpa,
        BigDecimal min10th,
        BigDecimal min12th,
        Integer maxBacklogs,
        Set<String> allowedBranches
) {
}
