=package com.ddd.demo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsername.UniqueUsernameValidator.class)
@Documented
public @interface UniqueUsername {
    String message() default "Username already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {
        @Override
        public boolean isValid(String username, ConstraintValidatorContext context) {
            // Implementation would check database for username uniqueness
            // For now, return true - actual implementation would use UserRepository
            return true;
        }
    }
}
