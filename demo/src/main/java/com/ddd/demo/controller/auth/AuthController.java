package com.ddd.demo.controller.auth;

import com.ddd.demo.common.response.ApiResponse;
import com.ddd.demo.dto.auth.*;
import com.ddd.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    // Register new user
    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful"));
    }

    // Login
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with username/email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    // Refresh token
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Logout
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout", description = "Logout current user")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            @RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    // Change password
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Change current user password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    // Forgot password
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset email sent"));
    }

    // Reset password
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    // Verify email
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully"));
    }

    // Resend verification email
    @PostMapping("/resend-verification")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Resend verification", description = "Resend email verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestAttribute("userId") Long userId) {
        authService.resendVerificationEmail(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Verification email sent"));
    }
}