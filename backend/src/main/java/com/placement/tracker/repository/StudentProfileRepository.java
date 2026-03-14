package com.placement.tracker.repository;

import com.placement.tracker.model.entity.StudentProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository for StudentProfile entity data access.
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    // Spring Data derives query: select sp from StudentProfile sp where sp.student.id = ?1
    Optional<StudentProfile> findByStudentId(Long studentId);
}
