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
@Constraint(validatedBy = UniqueUsername.UniqueUsernameValidator.class)
@Documented
public @interface UniqueUsername {
    String message() default "Username already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Component
    class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

        @Autowired
        private DatabaseValidationService validationService;

        @Override
        public boolean isValid(String username, ConstraintValidatorContext context) {
            if (!StringUtils.hasText(username)) {
                return true; // Let @NotBlank handle null/empty validation
            }

            try {
                return !validationService.usernameExists(username);
            } catch (Exception e) {
                // Log error and return true to avoid blocking registration due to database issues
                return true;
            }
        }
    }
}