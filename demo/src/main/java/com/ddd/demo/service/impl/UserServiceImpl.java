package com.ddd.demo.service.impl;

import com.ddd.demo.common.exception.BusinessException;
import com.ddd.demo.common.exception.ResourceNotFoundException;
import com.ddd.demo.dto.user.UserCreateRequest;
import com.ddd.demo.dto.user.UserResponse;
import com.ddd.demo.dto.user.UserUpdateRequest;
import com.ddd.demo.entity.user.User;
import com.ddd.demo.repository.UserRepository;
import com.ddd.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(UserCreateRequest request) {
        // Validate unique constraints
        if (existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        if (existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : User.UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created new user: {}", savedUser.getUsername());

        return mapToResponse(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Override
    @Cacheable(value = "users", key = "#username")
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update only provided fields
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user: {}", updatedUser.getUsername());

        return mapToResponse(updatedUser);
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        Page<User> users = keyword == null || keyword.isEmpty()
                ? userRepository.findByIsActiveTrue(pageable)
                : userRepository.searchUsers(keyword, pageable);

        return users.map(this::mapToResponse);
    }

    @Override
    public Page<UserResponse> getActiveUsers(Pageable pageable) {
        return userRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(false);
        user.setIsDeleted(true);
        userRepository.save(user);

        log.info("Soft deleted user: {}", user.getUsername());
    }

    @Override
    public List<UserResponse> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void updateUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(isActive);
        userRepository.save(user);

        log.info("User {} status updated to: {}", user.getUsername(), isActive);
    }

    // Helper method to map entity to response
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}