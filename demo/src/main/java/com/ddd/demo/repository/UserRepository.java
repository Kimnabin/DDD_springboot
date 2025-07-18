package com.ddd.demo.repository;

import com.ddd.demo.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by username - using Optional for null safety
    Optional<User> findByUsername(String username);

    // Find by email
    Optional<User> findByEmail(String email);

    // Check existence more efficiently
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Find active users with pagination
    Page<User> findByIsActiveTrue(Pageable pageable);

    // Custom query with index hint for better performance
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);

    // Bulk update for better performance
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.lastLoginDate < :date")
    int deactivateInactiveUsers(@Param("date") LocalDateTime date);

    // Find users by role
    List<User> findByRole(User.UserRole role);

    // Search users with keyword
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}