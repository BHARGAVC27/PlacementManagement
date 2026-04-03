package com.placement.tracker.repository;

import com.placement.tracker.model.entity.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// This repository handles database access for Student entities.
// It keeps data access separate from service/business logic.
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Spring Data derives query: select s from Student s where s.usn = ?1
    Optional<Student> findByUsn(String usn);

    // NEW: Find student by their email (inherited from User parent class)
    // Student extends User, and email is stored in the users table
    Optional<Student> findByEmail(String email);

    // Custom JPQL query joins StudentProfile and returns students by branch.
    @Query("select s from StudentProfile sp join sp.student s where sp.branch = :branch")
    List<Student> findByBranch(String branch);
}
