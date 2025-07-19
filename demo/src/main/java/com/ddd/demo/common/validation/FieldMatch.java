package com.ddd.demo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.lang.reflect.Field;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldMatch.FieldMatchValidator.class)
@Documented
public @interface FieldMatch {
    String message() default "Fields do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String first();
    String second();

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        FieldMatch[] value();
    }

    class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
        private String firstFieldName;
        private String secondFieldName;
        private String message;

        @Override
        public void initialize(FieldMatch constraintAnnotation) {
            firstFieldName = constraintAnnotation.first();
            secondFieldName = constraintAnnotation.second();
            message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }

            try {
                Object firstObj = getFieldValue(value, firstFieldName);
                Object secondObj = getFieldValue(value, secondFieldName);

                boolean valid = (firstObj == null && secondObj == null) ||
                        (firstObj != null && firstObj.equals(secondObj));

                if (!valid) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(message)
                            .addPropertyNode(secondFieldName)
                            .addConstraintViolation();
                }

                return valid;
            } catch (Exception e) {
                return false;
            }
        }

        private Object getFieldValue(Object object, String fieldName) throws Exception {
            Class<?> clazz = object.getClass();
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        }
    }
}