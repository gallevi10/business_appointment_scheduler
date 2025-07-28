package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// This interface defines the repository for managing User entities.
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // retrieves a user by their username
    Optional<User> findByUsername(String username);

    // checks if a user exists by their username
    boolean existsByUsername(String username);
}
