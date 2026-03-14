package com.placement.tracker.repository;

import com.placement.tracker.model.entity.JobRound;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// This repository handles database access for JobRound entities.
// Use it to fetch rounds linked to a particular job post.
public interface JobRoundRepository extends JpaRepository<JobRound, Long> {

    // Spring Data derives query: select r from JobRound r where r.jobPost.id = ?1
    List<JobRound> findByJobPostId(Long jobPostId);
}
