package com.ddd.demo.dto.user;

import com.ddd.demo.common.validation.StrongPassword;
import com.ddd.demo.common.validation.ValidPhoneNumber;
import com.ddd.demo.common.validation.ValidEnum;
import com.ddd.demo.entity.user.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscore")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @StrongPassword(
            minLength = 6,
            requireUppercase = true,
            requireLowercase = true,
            requireDigit = true,
            requireSpecialChar = true,
            message = "Password must meet security requirements"
    )
    private String password;

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]+$", message = "Full name can only contain letters and spaces")
    private String fullName;

    @ValidPhoneNumber(required = false, message = "Please provide a valid Vietnamese phone number")
    private String phoneNumber;

    @ValidEnum(enumClass = User.UserRole.class, message = "Invalid user role")
    private String role;
}
