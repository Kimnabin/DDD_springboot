package com.ddd.demo.service;

import com.ddd.demo.dto.user.UserCreateRequest;
import com.ddd.demo.dto.user.UserResponse;
import com.ddd.demo.dto.user.UserUpdateRequest;
import com.ddd.demo.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    // Create new user
    UserResponse createUser(UserCreateRequest request);

    // Get user by ID
    UserResponse getUserById(Long id);

    // Get user by username
    UserResponse getUserByUsername(String username);

    // Update user
    UserResponse updateUser(Long id, UserUpdateRequest request);

    // Search users with pagination
    Page<UserResponse> searchUsers(String keyword, Pageable pageable);

    // Get all active users
    Page<UserResponse> getActiveUsers(Pageable pageable);

    // Delete user (soft delete)
    void deleteUser(Long id);

    // Get users by role
    List<UserResponse> getUsersByRole(User.UserRole role);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Change user password
    void changePassword(Long userId, String oldPassword, String newPassword);

    // Activate/Deactivate user
    void updateUserStatus(Long userId, boolean isActive);
}