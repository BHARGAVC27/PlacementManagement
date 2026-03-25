package com.placement.tracker.service;

import com.placement.tracker.dto.ApplicationResponse;
import com.placement.tracker.enums.AppStatus;
import com.placement.tracker.enums.JobStatus;
import com.placement.tracker.model.entity.Application;
import com.placement.tracker.model.entity.JobPost;
import com.placement.tracker.model.entity.Student;
import com.placement.tracker.model.entity.StudentProfile;
import com.placement.tracker.repository.ApplicationRepository;
import com.placement.tracker.repository.JobPostRepository;
import com.placement.tracker.repository.StudentProfileRepository;
import com.placement.tracker.repository.StudentRepository;
import com.placement.tracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final JobPostRepository jobPostRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            StudentRepository studentRepository,
            JobPostRepository jobPostRepository,
            StudentProfileRepository studentProfileRepository,
            UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.jobPostRepository = jobPostRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
    }

    public ApplicationResponse apply(String email, Long jobId) {
        Long studentId = getStudentIdByEmail(email);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() ->
                    new NoSuchElementException("Student not found"));
        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() ->
                    new NoSuchElementException("Job post not found"));
        StudentProfile profile =
            studentProfileRepository.findByStudentId(studentId)
                .orElseThrow(() ->
                    new NoSuchElementException("Student profile not found"));

        if (jobPost.getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException(
                "Applications are closed for this job");
        }

        boolean alreadyApplied =
            applicationRepository.findByStudentId(studentId)
                .stream()
                .anyMatch(a -> a.getJobPost().getId().equals(jobId));
        if (alreadyApplied) {
            throw new IllegalArgumentException(
                "Student already applied to this job");
        }

        if (!isEligible(profile, jobPost)) {
            throw new IllegalArgumentException(
                "Student is not eligible for this job");
        }

        Application application = new Application();
        application.setAppliedDate(LocalDateTime.now());
        application.setCurrentStatus(AppStatus.APPLIED);
        application.setRemarks(null);
        application.setStudent(student);
        application.setJobPost(jobPost);

        Application saved = applicationRepository.save(application);
        return toResponse(saved);
    }

    public ApplicationResponse updateStatus(Long appId,
                                             AppStatus status,
                                             String remarks) {
        Application application = applicationRepository.findById(appId)
                .orElseThrow(() ->
                    new NoSuchElementException("Application not found"));

        application.setCurrentStatus(status);
        if (remarks != null && !remarks.isEmpty()) {
            application.setRemarks(remarks);
        }
        Application saved = applicationRepository.save(application);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getByJob(Long jobId) {
        return applicationRepository.findByJobPostId(jobId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getByStudent(String email) {
        Long studentId = getStudentIdByEmail(email);
        return applicationRepository.findByStudentId(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Long getStudentIdByEmail(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new NoSuchElementException("User not found"))
                .getId();
        Student student = studentRepository.findById(userId)
                .orElseThrow(() ->
                    new NoSuchElementException("Student not found"));
        return student.getId();
    }

    private boolean isEligible(StudentProfile profile, JobPost jobPost) {
        boolean cgpaOk =
            compare(profile.getCurrentCgpa(), jobPost.getMinCgpa()) >= 0;
        boolean tenthOk =
            compare(profile.getTenthPercent(), jobPost.getMin10th()) >= 0;
        boolean twelfthOk =
            compare(profile.getTwelfthPercent(), jobPost.getMin12th()) >= 0;
        boolean backlogOk =
            profile.getActiveBacklogs() <= jobPost.getMaxBacklogs();
        Set<String> allowed = jobPost.getAllowedBranches() == null
            ? Set.of() : jobPost.getAllowedBranches();
        boolean branchOk =
            allowed.isEmpty() || allowed.contains(profile.getBranch());
        return cgpaOk && tenthOk && twelfthOk && backlogOk && branchOk;
    }

    private int compare(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;
        return left.compareTo(right);
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getStudent().getId(),
                application.getJobPost().getId(),
                application.getJobPost().getCompanyName(),
                application.getCurrentStatus(),
                application.getAppliedDate(),
                application.getRemarks() == null
                    ? "" : application.getRemarks()
        );
    }
}