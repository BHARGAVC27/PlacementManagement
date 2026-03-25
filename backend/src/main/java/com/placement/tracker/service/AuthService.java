package com.placement.tracker.service;

import com.placement.tracker.dto.AuthRequest;
import com.placement.tracker.dto.AuthResponse;
import com.placement.tracker.dto.StudentRegisterRequest;
import com.placement.tracker.enums.Role;
import com.placement.tracker.model.entity.Student;
import com.placement.tracker.model.entity.StudentProfile;
import com.placement.tracker.model.entity.User;
import com.placement.tracker.repository.StudentProfileRepository;
import com.placement.tracker.repository.StudentRepository;
import com.placement.tracker.repository.UserRepository;
import com.placement.tracker.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       StudentProfileRepository studentProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Full student registration with profile details.
    public AuthResponse registerStudent(StudentRegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new IllegalArgumentException("Email already registered");
        });

        Student student = new Student();
        student.setEmail(request.email());
        student.setPasswordHash(passwordEncoder.encode(request.password()));
        student.setRole(Role.STUDENT);
        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setUsn(request.usn());
        student.setPhone(request.phone());
        Student savedStudent = studentRepository.save(student);

        StudentProfile profile = new StudentProfile();
        profile.setCurrentCgpa(request.currentCgpa());
        profile.setTenthPercent(request.tenthPercent());
        profile.setTwelfthPercent(request.twelfthPercent());
        profile.setActiveBacklogs(request.activeBacklogs() == null ? 0 : request.activeBacklogs());
        profile.setBranch(request.branch());
        profile.setResumeUrl(request.resumeUrl() == null ? "" : request.resumeUrl());
        profile.setStudent(savedStudent);
        studentProfileRepository.save(profile);

        String token = jwtUtil.generateToken(savedStudent.getEmail(), savedStudent.getRole());
        return new AuthResponse(token, savedStudent.getRole(), savedStudent.getEmail());
    }

    // Basic register (kept for compatibility).
    public AuthResponse register(AuthRequest request, Role role) {
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new IllegalArgumentException("Email already registered");
        });
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());
        return new AuthResponse(token, savedUser.getRole(), savedUser.getEmail());
    }

    // Login.
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getRole(), user.getEmail());
    }
}