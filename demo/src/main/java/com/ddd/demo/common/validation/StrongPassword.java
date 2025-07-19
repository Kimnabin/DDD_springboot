package com.ddd.demo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.util.StringUtils;

import java.lang.annotation.*;
import java.util.regex.Pattern;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPassword.PasswordValidator.class)
@Documented
public @interface StrongPassword {
    String message() default "Password must contain at least 8 characters with uppercase, lowercase, digit and special character";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int minLength() default 8;
    boolean requireUppercase() default true;
    boolean requireLowercase() default true;
    boolean requireDigit() default true;
    boolean requireSpecialChar() default true;

    class PasswordValidator implements ConstraintValidator<StrongPassword, String> {
        private int minLength;
        private boolean requireUppercase;
        private boolean requireLowercase;
        private boolean requireDigit;
        private boolean requireSpecialChar;

        @Override
        public void initialize(StrongPassword constraintAnnotation) {
            this.minLength = constraintAnnotation.minLength();
            this.requireUppercase = constraintAnnotation.requireUppercase();
            this.requireLowercase = constraintAnnotation.requireLowercase();
            this.requireDigit = constraintAnnotation.requireDigit();
            this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
        }

        @Override
        public boolean isValid(String password, ConstraintValidatorContext context) {
            if (!StringUtils.hasText(password)) {
                return true; // Let @NotBlank handle null/empty validation
            }

            // Check minimum length
            if (password.length() < minLength) {
                addCustomMessage(context, "Password must be at least " + minLength + " characters long");
                return false;
            }

            // Check for an uppercase letter
            if (requireUppercase && !password.matches(".*[A-Z].*")) {
                addCustomMessage(context, "Password must contain at least one uppercase letter");
                return false;
            }

            // Check for a lowercase letter
            if (requireLowercase && !password.matches(".*[a-z].*")) {
                addCustomMessage(context, "Password must contain at least one lowercase letter");
                return false;
            }

            // Check for a digit
            if (requireDigit && !password.matches(".*\\d.*")) {
                addCustomMessage(context, "Password must contain at least one digit");
                return false;
            }

            // Check for special character
            if (requireSpecialChar && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                addCustomMessage(context, "Password must contain at least one special character");
                return false;
            }

            return true;
        }

        private void addCustomMessage(ConstraintValidatorContext context, String message) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }
    }
}