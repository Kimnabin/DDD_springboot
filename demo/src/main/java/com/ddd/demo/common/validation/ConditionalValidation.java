package com.ddd.demo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.util.StringUtils;

import java.lang.annotation.*;
import java.lang.reflect.Field;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalValidation.ConditionalValidator.class)
@Documented
public @interface ConditionalValidation {
    String message() default "Conditional validation failed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String conditionalProperty();
    String[] requiredProperties();
    String[] conditionalValues() default {};

    class ConditionalValidator implements ConstraintValidator<ConditionalValidation, Object> {
        private String conditionalProperty;
        private String[] requiredProperties;
        private String[] conditionalValues;

        @Override
        public void initialize(ConditionalValidation constraintAnnotation) {
            conditionalProperty = constraintAnnotation.conditionalProperty();
            requiredProperties = constraintAnnotation.requiredProperties();
            conditionalValues = constraintAnnotation.conditionalValues();
        }

        @Override
        public boolean isValid(Object object, ConstraintValidatorContext context) {
            if (object == null) {
                return true;
            }

            try {
                Object conditionalPropertyValue = getPropertyValue(object, conditionalProperty);

                if (conditionalPropertyValue == null) {
                    return true;
                }

                String conditionalStringValue = conditionalPropertyValue.toString();

                // Check if conditional value matches any of the specified values
                boolean shouldValidate = conditionalValues.length == 0 ||
                        java.util.Arrays.asList(conditionalValues).contains(conditionalStringValue);

                if (!shouldValidate) {
                    return true;
                }

                // Validate required properties
                for (String requiredProperty : requiredProperties) {
                    Object requiredValue = getPropertyValue(object, requiredProperty);
                    if (requiredValue == null ||
                            (requiredValue instanceof String && !StringUtils.hasText((String) requiredValue))) {

                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate(
                                        requiredProperty + " is required when " + conditionalProperty + " is " + conditionalStringValue)
                                .addPropertyNode(requiredProperty)
                                .addConstraintViolation();
                        return false;
                    }
                }

                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private Object getPropertyValue(Object object, String propertyName) throws Exception {
            Field field = object.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            return field.get(object);
        }
    }
}