package com.ddd.demo.common.validation;

import com.ddd.demo.service.validation.DatabaseValidationService;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmail.UniqueEmailValidator.class)
@Documented
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Component
    class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

        @Autowired
        private DatabaseValidationService validationService;

        @Override
        public boolean isValid(String email, ConstraintValidatorContext context) {
            if (!StringUtils.hasText(email)) {
                return true; // Let @NotBlank handle null/empty validation
            }

            try {
                return !validationService.emailExists(email);
            } catch (Exception e) {
                // Log error and return true to avoid blocking registration due to database issues
                return true;
            }
        }
    }
}