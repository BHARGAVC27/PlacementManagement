package com.placement.tracker.controller;

import com.placement.tracker.enums.AppStatus;
import com.placement.tracker.model.entity.Application;
import com.placement.tracker.model.entity.StudentProfile;
import com.placement.tracker.repository.ApplicationRepository;
import com.placement.tracker.repository.JobPostRepository;
import com.placement.tracker.repository.StudentProfileRepository;
import com.placement.tracker.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Admin placement reports and analytics")
public class ReportsController {

    private final StudentRepository studentRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentProfileRepository studentProfileRepository;

    public ReportsController(StudentRepository studentRepository,
                             JobPostRepository jobPostRepository,
                             ApplicationRepository applicationRepository,
                             StudentProfileRepository studentProfileRepository) {
        this.studentRepository = studentRepository;
        this.jobPostRepository = jobPostRepository;
        this.applicationRepository = applicationRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PLACEMENT_OFFICER')")
    @Operation(summary = "Get full placement analytics summary")
    public ResponseEntity<Map<String, Object>> getSummary() {

        long totalStudents    = studentRepository.count();
        long totalJobs        = jobPostRepository.count();
        long totalApplied     = applicationRepository.findByCurrentStatus(AppStatus.APPLIED).size();
        long totalShortlisted = applicationRepository.findByCurrentStatus(AppStatus.SHORTLISTED_OA).size();
        long totalOACleared   = applicationRepository.findByCurrentStatus(AppStatus.OA_CLEARED).size();
        long totalInterview   = applicationRepository.findByCurrentStatus(AppStatus.INTERVIEW_SCHEDULED).size();
        long totalRejected    = applicationRepository.findByCurrentStatus(AppStatus.REJECTED).size();

        List<Application> offeredApps = applicationRepository.findByCurrentStatus(AppStatus.OFFERED);
        long totalOffered = offeredApps.size();

        // Placement % = students who got at least one offer / total students
        double placementPct = totalStudents > 0
                ? BigDecimal.valueOf(totalOffered * 100.0 / totalStudents)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // ── Company-wise offers (sorted by count desc)
        Map<String, Long> companyOffers = offeredApps.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getJobPost().getCompanyName(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        // ── Branch-wise offers
        Map<String, Long> branchOffers = offeredApps.stream()
                .collect(Collectors.groupingBy(app -> {
                    StudentProfile p = studentProfileRepository
                            .findByStudentId(app.getStudent().getId()).orElse(null);
                    return p != null ? p.getBranch() : "Unknown";
                }, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        // ── Branch-wise placement % (offered in branch / total students in branch)
        Map<String, Long> totalPerBranch = studentProfileRepository.findAll().stream()
                .collect(Collectors.groupingBy(StudentProfile::getBranch, Collectors.counting()));

        Map<String, String> branchPlacementPct = new LinkedHashMap<>();
        branchOffers.forEach((branch, offered) -> {
            long branchTotal = totalPerBranch.getOrDefault(branch, 1L);
            double pct = BigDecimal.valueOf(offered * 100.0 / branchTotal)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();
            branchPlacementPct.put(branch, pct + "%  (" + offered + "/" + branchTotal + ")");
        });

        // ── Package statistics from ALL job posts
        List<BigDecimal> packages = jobPostRepository.findAll().stream()
                .map(j -> j.getPackageLPA())
                .filter(p -> p != null)
                .sorted()
                .toList();

        BigDecimal avgPkg = BigDecimal.ZERO;
        BigDecimal maxPkg = BigDecimal.ZERO;
        BigDecimal minPkg = BigDecimal.ZERO;

        if (!packages.isEmpty()) {
            maxPkg = packages.get(packages.size() - 1);
            minPkg = packages.get(0);
            BigDecimal sum = packages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            avgPkg = sum.divide(BigDecimal.valueOf(packages.size()), 2, RoundingMode.HALF_UP);
        }

        // ── Top hiring companies (by number of job posts)
        Map<String, Long> topHiringCompanies = jobPostRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        j -> j.getCompanyName(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalStudents",       totalStudents);
        result.put("totalJobs",           totalJobs);
        result.put("totalApplied",        totalApplied);
        result.put("totalShortlisted",    totalShortlisted);
        result.put("totalOACleared",      totalOACleared);
        result.put("totalInterview",      totalInterview);
        result.put("totalOffered",        totalOffered);
        result.put("totalRejected",       totalRejected);
        result.put("placementPercent",    placementPct);
        result.put("companyOffers",       companyOffers);
        result.put("branchOffers",        branchOffers);
        result.put("branchPlacementPct",  branchPlacementPct);
        result.put("topHiringCompanies",  topHiringCompanies);
        result.put("avgPackageLPA",       avgPkg);
        result.put("maxPackageLPA",       maxPkg);
        result.put("minPackageLPA",       minPkg);

        return ResponseEntity.ok(result);
    }
}
