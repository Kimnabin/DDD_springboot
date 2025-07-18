package com.ddd.demo.controller.user;

import com.ddd.demo.common.response.ApiResponse;
import com.ddd.demo.common.response.PageResponse;
import com.ddd.demo.dto.user.UserCreateRequest;
import com.ddd.demo.dto.user.UserResponse;
import com.ddd.demo.dto.user.UserUpdateRequest;
import com.ddd.demo.entity.user.User;
import com.ddd.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    // Create new user - Admin only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user", description = "Create a new user account (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User created successfully"));
    }

    // Get user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // Get current user profile
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current logged-in user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    // Update user
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Update user", description = "Update user information")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    // Search users with pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users", description = "Search users with pagination (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserResponse> users = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(users)));
    }

    // Get users by role
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "Get all users with specific role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable User.UserRole role) {
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // Delete user (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Soft delete a user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    // Activate/Deactivate user
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Activate or deactivate user")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean isActive) {
        userService.updateUserStatus(id, isActive);
        String message = isActive ? "User activated successfully" : "User deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(null, message));
    }

    // Check if username exists
    @GetMapping("/check/username/{username}")
    @Operation(summary = "Check username", description = "Check if username already exists")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    // Check if email exists
    @GetMapping("/check/email/{email}")
    @Operation(summary = "Check email", description = "Check if email already exists")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}