package com.placement.tracker.controller;

import com.placement.tracker.dto.AuthRequest;
import com.placement.tracker.dto.AuthResponse;
import com.placement.tracker.dto.StudentRegisterRequest;
import com.placement.tracker.enums.Role;
import com.placement.tracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register basic user")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request, Role.STUDENT));
    }

    @PostMapping("/register/student")
    @Operation(summary = "Register student with full profile")
    public ResponseEntity<AuthResponse> registerStudent(
            @RequestBody StudentRegisterRequest request) {
        return ResponseEntity.ok(authService.registerStudent(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}