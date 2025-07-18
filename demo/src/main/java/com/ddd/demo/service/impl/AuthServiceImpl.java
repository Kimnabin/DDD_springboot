package com.ddd.demo.service.impl;

import com.ddd.demo.common.exception.BusinessException;
import com.ddd.demo.common.exception.ResourceNotFoundException;
import com.ddd.demo.dto.auth.*;
import com.ddd.demo.dto.user.UserCreateRequest;
import com.ddd.demo.dto.user.UserResponse;
import com.ddd.demo.entity.user.User;
import com.ddd.demo.repository.UserRepository;
import com.ddd.demo.security.JwtTokenProvider;
import com.ddd.demo.service.AuthService;
import com.ddd.demo.service.EmailService;
import com.ddd.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private static final String RESET_TOKEN_PREFIX = "reset:token:";
    private static final String VERIFY_TOKEN_PREFIX = "verify:token:";

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        // Create user
        UserCreateRequest userRequest = UserCreateRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .build();

        UserResponse user = userService.createUser(userRequest);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Send verification email
        sendVerificationEmail(user.getId(), user.getEmail());

        log.info("User registered successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .user(user)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        // Get user
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new BusinessException("User account is deactivated");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Store refresh token if remember me
        if (Boolean.TRUE.equals(request.getRememberMe())) {
            storeRefreshToken(user.getId(), refreshToken);
        }

        log.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .user(mapToUserResponse(user))
                .issuedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Invalid refresh token");
        }

        // Get user ID from token
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        log.info("Token refreshed for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .user(mapToUserResponse(user))
                .issuedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void logout(String token) {
        // Add token to blacklist
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true", accessTokenExpiration, TimeUnit.MILLISECONDS);

        log.info("User logged out successfully");
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // Validate new passwords match
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BusinessException("New passwords do not match");
        }

        // Change password through user service
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());

        log.info("Password changed for user ID: {}", userId);
    }

    @Override
    public void forgotPassword(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        String key = RESET_TOKEN_PREFIX + resetToken;

        // Store token in Redis with 24 hour expiration
        redisTemplate.opsForValue().set(key, user.getId().toString(), 24, TimeUnit.HOURS);

        // Send reset email
        emailService.sendPasswordResetEmail(email, resetToken);

        log.info("Password reset email sent to: {}", email);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        // Get user ID from token
        String key = RESET_TOKEN_PREFIX + request.getToken();
        String userIdStr = redisTemplate.opsForValue().get(key);

        if (userIdStr == null) {
            throw new BusinessException("Invalid or expired reset token");
        }

        // Update password
        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete token
        redisTemplate.delete(key);

        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    @Override
    public void verifyEmail(String token) {
        // Get user ID from token
        String key = VERIFY_TOKEN_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(key);

        if (userIdStr == null) {
            throw new BusinessException("Invalid or expired verification token");
        }

        // Update user email verified status
        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Mark email as verified (add field if needed)
        userRepository.save(user);

        // Delete token
        redisTemplate.delete(key);

        log.info("Email verified for user: {}", user.getUsername());
    }

    @Override
    public void resendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        sendVerificationEmail(userId, user.getEmail());

        log.info("Verification email resent to: {}", user.getEmail());
    }

    @Override
    public boolean validateToken(String token) {
        // Check if token is blacklisted
        String key = TOKEN_BLACKLIST_PREFIX + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return false;
        }

        return jwtTokenProvider.validateToken(token);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    // Helper methods
    private void sendVerificationEmail(Long userId, String email) {
        String verifyToken = UUID.randomUUID().toString();
        String key = VERIFY_TOKEN_PREFIX + verifyToken;

        // Store token with 48 hour expiration
        redisTemplate.opsForValue().set(key, userId.toString(), 48, TimeUnit.HOURS);

        // Send email
        emailService.sendTemplatedEmail(
                email,
                "Verify your email",
                "email/verify-email",
                Map.of("verifyLink", "http://localhost:8080/verify-email?token=" + verifyToken)
        );
    }

    private void storeRefreshToken(Long userId, String refreshToken) {
        String key = "refresh:token:" + userId;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}