package com.placement.tracker.service;

import com.placement.tracker.dto.JobRoundRequest;
import com.placement.tracker.dto.JobRoundResponse;
import com.placement.tracker.model.entity.JobPost;
import com.placement.tracker.model.entity.JobRound;
import com.placement.tracker.repository.JobPostRepository;
import com.placement.tracker.repository.JobRoundRepository;
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

    public JobRoundService(JobRoundRepository jobRoundRepository, JobPostRepository jobPostRepository) {
        this.jobRoundRepository = jobRoundRepository;
        this.jobPostRepository = jobPostRepository;
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

    private JobRoundResponse toResponse(JobRound round) {
        return new JobRoundResponse(
                round.getId(),
                round.getRoundName(),
                round.getScheduledTime(),
                round.getVenueOrLink(),
                round.getInstructions(),
                round.getJobPost().getId()
        );
    }
}
