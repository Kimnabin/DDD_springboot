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
import java.util.Map;
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
        validatePasswordsMatch(request.getPassword(), request.getConfirmPassword());

        UserCreateRequest userRequest = UserCreateRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .build();

        UserResponse user = userService.createUser(userRequest);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        sendVerificationEmail(user.getId(), user.getEmail());
        log.info("User registered successfully: {}", user.getUsername());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateUserActive(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        if (Boolean.TRUE.equals(request.getRememberMe())) {
            storeRefreshToken(user.getId(), refreshToken);
        }

        log.info("User logged in successfully: {}", user.getUsername());
        return buildAuthResponse(accessToken, refreshToken, mapToUserResponse(user));
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        validateRefreshToken(refreshToken);

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        log.info("Token refreshed for user: {}", user.getUsername());
        return buildAuthResponse(newAccessToken, newRefreshToken, mapToUserResponse(user));
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void logout(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true", accessTokenExpiration, TimeUnit.MILLISECONDS);
        log.info("User logged out successfully");
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        validatePasswordsMatch(request.getNewPassword(), request.getConfirmNewPassword());
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        log.info("Password changed for user ID: {}", userId);
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String resetToken = UUID.randomUUID().toString();
        String key = RESET_TOKEN_PREFIX + resetToken;

        redisTemplate.opsForValue().set(key, user.getId().toString(), 24, TimeUnit.HOURS);
        emailService.sendPasswordResetEmail(email, resetToken);
        log.info("Password reset email sent to: {}", email);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        validatePasswordsMatch(request.getNewPassword(), request.getConfirmPassword());

        String key = RESET_TOKEN_PREFIX + request.getToken();
        String userIdStr = redisTemplate.opsForValue().get(key);

        if (userIdStr == null) {
            throw new BusinessException("Invalid or expired reset token");
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisTemplate.delete(key);

        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    @Override
    public void verifyEmail(String token) {
        String key = VERIFY_TOKEN_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(key);

        if (userIdStr == null) {
            throw new BusinessException("Invalid or expired verification token");
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.save(user);
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

    // Private helper methods
    private void validatePasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException("Passwords do not match");
        }
    }

    private void validateUserActive(User user) {
        if (!user.getIsActive()) {
            throw new BusinessException("User account is deactivated");
        }
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Invalid refresh token");
        }
    }

    private void sendVerificationEmail(Long userId, String email) {
        String verifyToken = UUID.randomUUID().toString();
        String key = VERIFY_TOKEN_PREFIX + verifyToken;

        redisTemplate.opsForValue().set(key, userId.toString(), 48, TimeUnit.HOURS);

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

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, UserResponse user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .user(user)
                .issuedAt(LocalDateTime.now())
                .build();
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