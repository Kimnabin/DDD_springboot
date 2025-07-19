package com.ddd.demo.controller.user;

import com.ddd.demo.common.response.ApiResponse;
import com.ddd.demo.common.validation.ValidationGroups;
import com.ddd.demo.dto.user.UserCreateRequest;
import com.ddd.demo.dto.user.UserResponse;
import com.ddd.demo.dto.user.UserUpdateRequest;
import com.ddd.demo.service.UserService;
import com.ddd.demo.service.validation.BusinessValidationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated // Enable method-level validation
public class UserController {

    private final UserService userService;
    private final BusinessValidationService businessValidationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user", description = "Create a new user account (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {

        // Additional business validation
        List<String> businessErrors = businessValidationService.validateUserRegistration(
                request.getUsername(), request.getEmail());

        if (!businessErrors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Validation failed", String.join(", ", businessErrors)));
        }

        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable @Min(value = 1, message = "User ID must be positive") Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Update user", description = "Update user information")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable @Min(value = 1, message = "User ID must be positive") Long id,
            @Validated(ValidationGroups.UserUpdate.class) @RequestBody UserUpdateRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @GetMapping("/check/username/{username}")
    @Operation(summary = "Check username availability", description = "Check if username is available")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(
            @PathVariable @NotBlank(message = "Username cannot be blank")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(!exists));
    }
}