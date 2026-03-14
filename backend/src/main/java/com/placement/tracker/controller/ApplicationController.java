package com.placement.tracker.controller;

import com.placement.tracker.dto.ApplicationRequest;
import com.placement.tracker.dto.ApplicationResponse;
import com.placement.tracker.enums.AppStatus;
import com.placement.tracker.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// REST controller for application APIs.
@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Application APIs")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // Applies current logged-in student to a job post.
    @PostMapping
    @Operation(summary = "Apply to a job")
    public ResponseEntity<ApplicationResponse> apply(@AuthenticationPrincipal UserDetails principal,
                                                     @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.apply(principal.getUsername(), request.jobPostId()));
    }

    // Returns applications of currently logged-in student.
    @GetMapping("/my")
    @Operation(summary = "Get my applications")
    public ResponseEntity<List<ApplicationResponse>> getMy(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(applicationService.getByStudent(principal.getUsername()));
    }

    // Updates the status of one application.
    @PutMapping("/{id}/status")
    @Operation(summary = "Update application status")
    public ResponseEntity<ApplicationResponse> updateStatus(@PathVariable Long id,
                                                            @RequestParam AppStatus status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }
}
