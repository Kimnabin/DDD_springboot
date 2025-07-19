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
@Constraint(validatedBy = ValidSKU.SKUValidator.class)
@Documented
public @interface ValidSKU {
    String message() default "SKU must contain only uppercase letters, numbers and hyphens";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int minLength() default 3;
    int maxLength() default 50;

    class SKUValidator implements ConstraintValidator<ValidSKU, String> {
        private static final String SKU_PATTERN = "^[A-Z0-9\\-]+$";
        private static final Pattern pattern = Pattern.compile(SKU_PATTERN);

        private int minLength;
        private int maxLength;

        @Override
        public void initialize(ValidSKU constraintAnnotation) {
            this.minLength = constraintAnnotation.minLength();
            this.maxLength = constraintAnnotation.maxLength();
        }

        @Override
        public boolean isValid(String sku, ConstraintValidatorContext context) {
            if (!StringUtils.hasText(sku)) {
                return true; // Let @NotBlank handle null/empty validation
            }

            // Check length
            if (sku.length() < minLength || sku.length() > maxLength) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                                "SKU must be between " + minLength + " and " + maxLength + " characters")
                        .addConstraintViolation();
                return false;
            }

            // Check pattern
            if (!pattern.matcher(sku).matches()) {
                return false;
            }

            return true;
        }
    }
}