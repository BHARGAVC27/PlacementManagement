package com.placement.tracker.repository;

import com.placement.tracker.model.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// This repository handles database access for User entities.
// Extending JpaRepository gives CRUD, pagination, and sorting methods automatically.
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data derives query: select u from User u where u.email = ?1
    Optional<User> findByEmail(String email);
}
