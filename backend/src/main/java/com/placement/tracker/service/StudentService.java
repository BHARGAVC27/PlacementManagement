package com.placement.tracker.service;

import com.placement.tracker.dto.ApplicationResponse;
import com.placement.tracker.dto.StudentProfileDTO;
import com.placement.tracker.dto.StudentSummaryDTO;
import com.placement.tracker.model.entity.Application;
import com.placement.tracker.model.entity.Student;
import com.placement.tracker.model.entity.StudentProfile;
import com.placement.tracker.repository.ApplicationRepository;
import com.placement.tracker.repository.StudentProfileRepository;
import com.placement.tracker.repository.StudentRepository;
import com.placement.tracker.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service layer for student profile and student-centric application operations.
@Service
@Transactional
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public StudentService(StudentProfileRepository studentProfileRepository,
                          ApplicationRepository applicationRepository,
                          UserRepository userRepository,
                          StudentRepository studentRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    // Returns personal info (name, email, usn, phone) for the logged-in student.
    @Transactional(readOnly = true)
    public Map<String, String> getPersonalInfo(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Student not found: " + email));
        return Map.of(
                "firstName",  student.getFirstName() != null  ? student.getFirstName()  : "",
                "lastName",   student.getLastName()  != null  ? student.getLastName()   : "",
                "email",      student.getEmail()     != null  ? student.getEmail()      : "",
                "usn",        student.getUsn()       != null  ? student.getUsn()        : "",
                "phone",      student.getPhone()     != null  ? student.getPhone()      : ""
        );
    }

    // Steps: resolve student from email -> fetch profile -> map entity to DTO.
    @Transactional(readOnly = true)
    public StudentProfileDTO getProfile(String email) {
        Long studentId = getStudentIdByEmail(email);
        StudentProfile profile = studentProfileRepository.findByStudentId(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student profile not found"));
        return toProfileDTO(profile);
    }

    // Steps: resolve student from email -> update profile fields -> save -> return DTO.
    public StudentProfileDTO updateProfile(String email, StudentProfileDTO dto) {
        Long studentId = getStudentIdByEmail(email);
        StudentProfile profile = studentProfileRepository.findByStudentId(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student profile not found"));
        profile.setCurrentCgpa(dto.currentCgpa());
        profile.setTenthPercent(dto.tenthPercent());
        profile.setTwelfthPercent(dto.twelfthPercent());
        profile.setActiveBacklogs(dto.activeBacklogs());
        profile.setBranch(dto.branch());
        profile.setResumeUrl(dto.resumeUrl());
        StudentProfile saved = studentProfileRepository.save(profile);
        return toProfileDTO(saved);
    }

    // Steps: resolve student from email -> fetch applications -> map to DTO list.
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(String email) {
        Long studentId = getStudentIdByEmail(email);
        return applicationRepository.findByStudentId(studentId)
                .stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    // Admin view — fetches ALL students with their profile data.
    // Students without a profile still appear, with N/A for academic fields.
    @Transactional(readOnly = true)
    public List<StudentSummaryDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(student -> {
                    StudentProfile profile = studentProfileRepository
                            .findByStudentId(student.getId())
                            .orElse(null);
                    return new StudentSummaryDTO(
                            student.getId(),
                            student.getFirstName(),
                            student.getLastName(),
                            student.getEmail(),
                            student.getUsn(),
                            profile != null ? profile.getBranch()         : "N/A",
                            profile != null ? profile.getCurrentCgpa()    : null,
                            profile != null ? profile.getTenthPercent()   : null,
                            profile != null ? profile.getTwelfthPercent() : null,
                            profile != null ? profile.getActiveBacklogs() : null
                    );
                })
                .toList();
    }

    private Long getStudentIdByEmail(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"))
                .getId();
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));
        return student.getId();
    }

    private StudentProfileDTO toProfileDTO(StudentProfile profile) {
        return new StudentProfileDTO(
                profile.getCurrentCgpa(),
                profile.getTenthPercent(),
                profile.getTwelfthPercent(),
                profile.getActiveBacklogs(),
                profile.getBranch(),
                profile.getResumeUrl()
        );
    }

    private ApplicationResponse toApplicationResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getStudent().getId(),
                application.getJobPost().getId(),
                application.getJobPost().getCompanyName(),
                application.getCurrentStatus(),
                application.getAppliedDate(),
                application.getRemarks()
        );
    }
}
