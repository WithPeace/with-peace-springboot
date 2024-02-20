package com.example.withpeace.annotation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LocalDateValidator.class)
@Target({ElementType.PARAMETER,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Date {
    String message() default "Invalid Date Format. (yyyy-MM-dd)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
