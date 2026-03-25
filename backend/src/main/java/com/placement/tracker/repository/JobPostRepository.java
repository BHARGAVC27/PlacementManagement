package com.placement.tracker.repository;

import com.placement.tracker.enums.JobStatus;
import com.placement.tracker.model.entity.JobPost;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// This repository handles database access for JobPost entities.
// Method names are converted into SQL/JPQL automatically by Spring Data.
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    // Spring Data derives query: select j from JobPost j where j.status = ?1
    List<JobPost> findByStatus(JobStatus status);

    // Spring Data derives query: select j from JobPost j where j.deadline > ?1
    List<JobPost> findByDeadlineAfter(LocalDate deadline);
}
