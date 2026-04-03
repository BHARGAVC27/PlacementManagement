package com.placement.tracker.controller;

import com.placement.tracker.dto.StudentProfileDTO;
import com.placement.tracker.dto.StudentSummaryDTO;
import com.placement.tracker.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// REST controller for student profile APIs.
@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student profile APIs")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Returns current logged-in student's personal info (name, email, usn, phone).
    @GetMapping("/me")
    @Operation(summary = "Get my personal info")
    public ResponseEntity<Map<String, String>> getMe(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(studentService.getPersonalInfo(principal.getUsername()));
    }

    // Returns current logged-in student's academic profile.
    @GetMapping("/profile")
    @Operation(summary = "Get my profile")
    public ResponseEntity<StudentProfileDTO> getProfile(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(studentService.getProfile(principal.getUsername()));
    }

    // Updates current logged-in student's academic profile.
    @PutMapping("/profile")
    @Operation(summary = "Update my profile")
    public ResponseEntity<StudentProfileDTO> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody StudentProfileDTO dto) {
        return ResponseEntity.ok(
                studentService.updateProfile(principal.getUsername(), dto));
    }

    // Admin-only endpoint — returns all students with their profile data.
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PLACEMENT_OFFICER')")
    @Operation(summary = "Get all students (admin only)")
    public ResponseEntity<List<StudentSummaryDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }
}
