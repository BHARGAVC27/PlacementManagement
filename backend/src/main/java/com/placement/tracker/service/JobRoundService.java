package com.placement.tracker.service;

import com.placement.tracker.dto.JobRoundRequest;
import com.placement.tracker.dto.JobRoundResponse;
import com.placement.tracker.enums.AppStatus;
import com.placement.tracker.model.entity.Application;
import com.placement.tracker.model.entity.JobPost;
import com.placement.tracker.model.entity.JobRound;
import com.placement.tracker.model.entity.Student;
import com.placement.tracker.repository.ApplicationRepository;
import com.placement.tracker.repository.JobPostRepository;
import com.placement.tracker.repository.JobRoundRepository;
import com.placement.tracker.repository.StudentRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service layer for creating and listing job rounds.
@Service
@Transactional
public class JobRoundService {

    private final JobRoundRepository jobRoundRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;

    public JobRoundService(JobRoundRepository jobRoundRepository,
                           JobPostRepository jobPostRepository,
                           ApplicationRepository applicationRepository,
                           StudentRepository studentRepository) {
        this.jobRoundRepository = jobRoundRepository;
        this.jobPostRepository = jobPostRepository;
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
    }

    // Steps: load job post -> map request to entity -> save round -> return DTO.
    public JobRoundResponse createRound(JobRoundRequest request) {
        JobPost jobPost = jobPostRepository.findById(request.jobPostId())
                .orElseThrow(() -> new NoSuchElementException("Job post not found"));

        JobRound round = new JobRound();
        round.setRoundName(request.roundName());
        round.setScheduledTime(request.scheduledTime());
        round.setVenueOrLink(request.venueOrLink());
        round.setInstructions(request.instructions());
        round.setJobPost(jobPost);

        JobRound saved = jobRoundRepository.save(round);
        return toResponse(saved);
    }

    // Steps: fetch all rounds by job id -> map entities to DTOs -> return list.
    @Transactional(readOnly = true)
    public List<JobRoundResponse> listByJob(Long jobId) {
        return jobRoundRepository.findByJobPostId(jobId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // NEW: Returns interview rounds ONLY for jobs where this student has
    // INTERVIEW_SCHEDULED status. Student is found by their email from JWT.
    @Transactional(readOnly = true)
    public List<JobRoundResponse> getInterviewRoundsForStudent(String email) {
        // Step 1: Find the student by email (email is inherited from User parent)
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Student not found for email: " + email));

        // Step 2: Find all applications for this student with INTERVIEW_SCHEDULED status
        List<Application> scheduledApps = applicationRepository
                .findByStudentIdAndCurrentStatus(student.getId(), AppStatus.INTERVIEW_SCHEDULED);

        // Step 3: For each shortlisted application, get all rounds for that job
        return scheduledApps.stream()
                .flatMap(app -> jobRoundRepository
                        .findByJobPostId(app.getJobPost().getId())
                        .stream())
                .map(this::toResponse)
                .toList();
    }

    private JobRoundResponse toResponse(JobRound round) {
        return new JobRoundResponse(
                round.getId(),
                round.getRoundName(),
                round.getScheduledTime(),
                round.getVenueOrLink(),
                round.getInstructions(),
                round.getJobPost().getId(),
                round.getJobPost().getCompanyName()
        );
    }

}
