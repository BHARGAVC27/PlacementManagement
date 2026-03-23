package com.placement.tracker.controller;

import com.placement.tracker.dto.EligibleStudentResponse;
import com.placement.tracker.dto.JobPostRequest;
import com.placement.tracker.dto.JobPostResponse;
import com.placement.tracker.enums.JobStatus;
import com.placement.tracker.service.JobPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Job post APIs")
public class JobPostController {

    private final JobPostService jobPostService;

    public JobPostController(JobPostService jobPostService) {
        this.jobPostService = jobPostService;
    }

    // Returns ALL jobs with their current status
    @GetMapping
    @Operation(summary = "Get all jobs")
    public ResponseEntity<List<JobPostResponse>> getAll() {
        return ResponseEntity.ok(jobPostService.getAll());
    }

    // Returns only OPEN jobs for student browsing
    @GetMapping("/open")
    @Operation(summary = "Get open jobs for students")
    public ResponseEntity<List<JobPostResponse>> getOpen() {
        return ResponseEntity.ok(jobPostService.getOpenJobs());
    }

    @PostMapping
    @Operation(summary = "Create job post")
    public ResponseEntity<JobPostResponse> create(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody JobPostRequest request) {
        return ResponseEntity.ok(jobPostService.create(request, principal.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by id")
    public ResponseEntity<JobPostResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostService.getJobById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job post")
    public ResponseEntity<JobPostResponse> update(
            @PathVariable Long id,
            @RequestBody JobPostRequest request) {
        return ResponseEntity.ok(jobPostService.update(id, request));
    }

    // Admin updates job status (OPEN -> ONGOING -> RESULTS_OUT -> CLOSED)
    @PutMapping("/{id}/status")
    @Operation(summary = "Update job status")
    public ResponseEntity<JobPostResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam JobStatus status) {
        return ResponseEntity.ok(jobPostService.updateStatus(id, status));
    }

    @GetMapping("/{id}/eligible-students")
    @Operation(summary = "Get eligible students for job")
    public ResponseEntity<List<EligibleStudentResponse>> getEligibleStudents(
            @PathVariable Long id) {
        return ResponseEntity.ok(jobPostService.getEligibleStudents(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job post")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobPostService.delete(id);
        return ResponseEntity.noContent().build();
    }
}