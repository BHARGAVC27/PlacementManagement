package com.placement.tracker.repository;

import com.placement.tracker.model.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository for Admin entity data access.
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
