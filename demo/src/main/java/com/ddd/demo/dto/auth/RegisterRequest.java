package com.ddd.demo.dto.auth;

import com.ddd.demo.common.validation.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldMatch(first = "password", second = "confirmPassword", message = "Password and confirm password must match")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscore")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @StrongPassword(
            minLength = 8,
            message = "Password must be at least 8 characters with uppercase, lowercase, digit and special character"
    )
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @ValidPhoneNumber(required = false, message = "Please provide a valid Vietnamese phone number")
    private String phoneNumber;

    @AssertTrue(message = "Terms and conditions must be accepted")
    private Boolean acceptTerms;
}