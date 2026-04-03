package com.placement.tracker.repository;

import com.placement.tracker.enums.AppStatus;
import com.placement.tracker.model.entity.Application;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// This repository handles database access for Application entities.
// It provides query methods for filtering applications by student, job, and status.
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Spring Data derives query: select a from Application a where a.student.id = ?1
    List<Application> findByStudentId(Long studentId);

    // Spring Data derives query: select a from Application a where a.jobPost.id = ?1
    List<Application> findByJobPostId(Long jobPostId);

    // Spring Data derives query: select a from Application a where a.currentStatus = ?1
    List<Application> findByCurrentStatus(AppStatus currentStatus);

    // NEW: Find all applications for a student with a specific status
    // Used to check which jobs a student has been shortlisted/scheduled for
    List<Application> findByStudentIdAndCurrentStatus(Long studentId, AppStatus currentStatus);
}
