package com.ddd.demo.service;

import com.ddd.demo.dto.auth.*;

public interface AuthService {

    // Register new user
    AuthResponse register(RegisterRequest request);

    // Login
    AuthResponse login(LoginRequest request);

    // Refresh token
    AuthResponse refreshToken(String refreshToken);

    // Logout
    void logout(String token);

    // Change password
    void changePassword(Long userId, ChangePasswordRequest request);

    // Forgot password
    void forgotPassword(String email);

    // Reset password
    void resetPassword(ResetPasswordRequest request);

    // Verify email
    void verifyEmail(String token);

    // Resend verification email
    void resendVerificationEmail(Long userId);

    // Validate token
    boolean validateToken(String token);

    // Get user ID from token
    Long getUserIdFromToken(String token);
}