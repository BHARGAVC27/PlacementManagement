package com.placement.tracker.service;

import com.placement.tracker.dto.EligibleStudentResponse;
import com.placement.tracker.dto.JobPostRequest;
import com.placement.tracker.dto.JobPostResponse;
import com.placement.tracker.enums.JobStatus;
import com.placement.tracker.model.entity.Admin;
import com.placement.tracker.model.entity.JobPost;
import com.placement.tracker.model.entity.StudentProfile;
import com.placement.tracker.repository.AdminRepository;
import com.placement.tracker.repository.JobPostRepository;
import com.placement.tracker.repository.StudentProfileRepository;
import com.placement.tracker.repository.UserRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobPostService {

    private final JobPostRepository jobPostRepository;
    private final AdminRepository adminRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public JobPostService(JobPostRepository jobPostRepository,
                          AdminRepository adminRepository,
                          StudentProfileRepository studentProfileRepository,
                          UserRepository userRepository) {
        this.jobPostRepository = jobPostRepository;
        this.adminRepository = adminRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
    }

    public JobPostResponse create(JobPostRequest request, String adminEmail) {
        Long userId = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found"))
                .getId();

        Admin admin = adminRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        JobPost jobPost = new JobPost();
        applyRequest(jobPost, request);
        jobPost.setAdmin(admin);

        JobPost saved = jobPostRepository.save(jobPost);
        return toJobPostResponse(saved);
    }

    public JobPostResponse update(Long id, JobPostRequest request) {
        JobPost existing = jobPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job post not found"));
        applyRequest(existing, request);
        JobPost saved = jobPostRepository.save(existing);
        return toJobPostResponse(saved);
    }

    // Returns ALL jobs so students can see status (OPEN, ONGOING, etc.)
    @Transactional(readOnly = true)
    public List<JobPostResponse> getAll() {
        return jobPostRepository.findAll()
                .stream()
                .map(this::toJobPostResponse)
                .toList();
    }

    // Returns only OPEN jobs for student application
    @Transactional(readOnly = true)
    public List<JobPostResponse> getOpenJobs() {
        return jobPostRepository.findByStatus(JobStatus.OPEN)
                .stream()
                .map(this::toJobPostResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobPostResponse getJobById(Long id) {
        JobPost jobPost = jobPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job post not found"));
        return toJobPostResponse(jobPost);
    }

    // Updates job status — used by admin to close applications, move to OA, etc.
    public JobPostResponse updateStatus(Long id, JobStatus status) {
        JobPost existing = jobPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job post not found"));
        existing.setStatus(status);
        JobPost saved = jobPostRepository.save(existing);
        return toJobPostResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EligibleStudentResponse> getEligibleStudents(Long jobId) {
        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job post not found"));

        return studentProfileRepository.findAll()
                .stream()
                .filter(profile -> isEligible(profile, jobPost))
                .map(profile -> new EligibleStudentResponse(
                        profile.getStudent().getId(),
                        profile.getStudent().getUsn(),
                        profile.getStudent().getFirstName(),
                        profile.getStudent().getLastName(),
                        profile.getBranch(),
                        profile.getCurrentCgpa(),
                        profile.getActiveBacklogs()
                ))
                .toList();
    }

    public void delete(Long id) {
        JobPost existing = jobPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job post not found"));
        jobPostRepository.delete(existing);
    }

    private void applyRequest(JobPost jobPost, JobPostRequest request) {
        jobPost.setCompanyName(request.companyName());
        jobPost.setRoleDescription(request.roleDescription());
        jobPost.setPackageLPA(request.packageLPA());
        jobPost.setDeadline(request.deadline());
        jobPost.setStatus(request.status() == null ? JobStatus.OPEN : request.status());
        jobPost.setMinCgpa(request.minCgpa());
        jobPost.setMin10th(request.min10th());
        jobPost.setMin12th(request.min12th());
        jobPost.setMaxBacklogs(request.maxBacklogs());
        Set<String> branches = request.allowedBranches() == null
                ? Set.of() : request.allowedBranches();
        jobPost.setAllowedBranches(new HashSet<>(branches));
    }

    private boolean isEligible(StudentProfile profile, JobPost jobPost) {
        boolean cgpaOk = compare(profile.getCurrentCgpa(), jobPost.getMinCgpa()) >= 0;
        boolean tenthOk = compare(profile.getTenthPercent(), jobPost.getMin10th()) >= 0;
        boolean twelfthOk = compare(profile.getTwelfthPercent(), jobPost.getMin12th()) >= 0;
        boolean backlogOk = profile.getActiveBacklogs() <= jobPost.getMaxBacklogs();
        Set<String> allowed = jobPost.getAllowedBranches() == null
                ? Set.of() : jobPost.getAllowedBranches();
        boolean branchOk = allowed.isEmpty() || allowed.contains(profile.getBranch());
        return cgpaOk && tenthOk && twelfthOk && backlogOk && branchOk;
    }

    private int compare(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;
        return left.compareTo(right);
    }

    private JobPostResponse toJobPostResponse(JobPost jobPost) {
        return new JobPostResponse(
                jobPost.getId(),
                jobPost.getCompanyName(),
                jobPost.getRoleDescription(),
                jobPost.getPackageLPA(),
                jobPost.getDeadline(),
                jobPost.getStatus(),
                jobPost.getMinCgpa(),
                jobPost.getMin10th(),
                jobPost.getMin12th(),
                jobPost.getMaxBacklogs(),
                jobPost.getAllowedBranches()
        );
    }
}