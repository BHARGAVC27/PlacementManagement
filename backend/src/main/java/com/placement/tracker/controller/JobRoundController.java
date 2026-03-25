package com.placement.tracker.controller;

import com.placement.tracker.dto.JobRoundRequest;
import com.placement.tracker.dto.JobRoundResponse;
import com.placement.tracker.service.JobRoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// REST controller for job round APIs.
@RestController
@RequestMapping("/api/rounds")
@Tag(name = "Rounds", description = "Job round APIs")
public class JobRoundController {

    private final JobRoundService jobRoundService;

    public JobRoundController(JobRoundService jobRoundService) {
        this.jobRoundService = jobRoundService;
    }

    // Creates a round under a job post.
    @PostMapping
    @Operation(summary = "Create round")
    public ResponseEntity<JobRoundResponse> create(@RequestBody JobRoundRequest request) {
        return ResponseEntity.ok(jobRoundService.createRound(request));
    }

    // Lists rounds for a specific job post id.
    @GetMapping("/job/{jobId}")
    @Operation(summary = "List rounds by job")
    public ResponseEntity<List<JobRoundResponse>> listByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobRoundService.listByJob(jobId));
    }
}
