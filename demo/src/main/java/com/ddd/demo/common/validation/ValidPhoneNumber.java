package com.ddd.demo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.util.StringUtils;

import java.lang.annotation.*;
import java.util.regex.Pattern;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPhoneNumber.PhoneNumberValidator.class)
@Documented
public @interface ValidPhoneNumber {
    String message() default "Phone number must be in format +84xxxxxxxxx or 0xxxxxxxxx";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    boolean required() default true;

    class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
        // Vietnamese phone number patterns
        private static final String VIETNAM_PHONE_PATTERN =
                "^(\\+84|84|0)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-6|8|9]|9[0-9])[0-9]{7}$";
        private static final Pattern pattern = Pattern.compile(VIETNAM_PHONE_PATTERN);

        private boolean required;

        @Override
        public void initialize(ValidPhoneNumber constraintAnnotation) {
            this.required = constraintAnnotation.required();
        }

        @Override
        public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
            // If not required and empty, it's valid
            if (!required && !StringUtils.hasText(phoneNumber)) {
                return true;
            }

            // If required and empty, it's invalid
            if (required && !StringUtils.hasText(phoneNumber)) {
                return false;
            }

            // Normalize phone number (remove spaces, dashes, parentheses)
            String normalizedPhone = phoneNumber.replaceAll("[\\s\\-()]", "");

            return pattern.matcher(normalizedPhone).matches();
        }
    }
}