package com.ddd.demo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.util.StringUtils;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidEnum.EnumValidator.class)
@Documented
public @interface ValidEnum {
    String message() default "Invalid enum value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> enumClass();
    boolean ignoreCase() default false;

    class EnumValidator implements ConstraintValidator<ValidEnum, String> {
        private Class<? extends Enum<?>> enumClass;
        private boolean ignoreCase;

        @Override
        public void initialize(ValidEnum constraintAnnotation) {
            this.enumClass = constraintAnnotation.enumClass();
            this.ignoreCase = constraintAnnotation.ignoreCase();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (!StringUtils.hasText(value)) {
                return true; // Let @NotBlank handle null/empty validation
            }

            Enum<?>[] enumValues = enumClass.getEnumConstants();
            for (Enum<?> enumValue : enumValues) {
                String enumName = enumValue.name();
                if (ignoreCase ? enumName.equalsIgnoreCase(value) : enumName.equals(value)) {
                    return true;
                }
            }

            // Create custom error message with valid values
            StringBuilder validValues = new StringBuilder();
            for (int i = 0; i < enumValues.length; i++) {
                validValues.append(enumValues[i].name());
                if (i < enumValues.length - 1) {
                    validValues.append(", ");
                }
            }

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Invalid value. Allowed values are: " + validValues.toString())
                    .addConstraintViolation();

            return false;
        }
    }
}
