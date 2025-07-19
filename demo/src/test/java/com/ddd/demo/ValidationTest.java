package com.ddd.demo;

import org.springframework.boot.test.context.SpringBootTest;
import com.ddd.demo.common.validation.ValidationUtils;
import com.ddd.demo.dto.auth.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ValidationTest {
    private Validator validator;
    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidRegisterRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser123")
                .email("test@example.com")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .fullName("Test User")
                .phoneNumber("0901234567")
                .acceptTerms(true)
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    public void testInvalidPassword() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser123")
                .email("test@example.com")
                .password("weak") // Too weak
                .confirmPassword("weak")
                .acceptTerms(true)
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Weak password should cause violations");

        boolean hasPasswordViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(hasPasswordViolation, "Should have password violation");
    }

    @Test
    public void testPasswordMismatch() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser123")
                .email("test@example.com")
                .password("SecurePass123!")
                .confirmPassword("DifferentPass123!")
                .acceptTerms(true)
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Password mismatch should cause violations");
    }

    @Test
    public void testValidationUtils() {
        // Test email validation
        assertTrue(ValidationUtils.isValidEmail("test@example.com"));
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));

        // Test phone validation
        assertTrue(ValidationUtils.isValidRange(100, 50, 150));
        assertFalse(ValidationUtils.isValidRange(200, 50, 150));

        // Test string length validation
        assertTrue(ValidationUtils.isValidStringLength("test", 2, 10));
        assertFalse(ValidationUtils.isValidStringLength("a", 2, 10));
    }
}
