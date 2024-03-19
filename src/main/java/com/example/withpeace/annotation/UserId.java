package com.example.withpeace.annotation;

import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(hidden = true)
public @interface UserId {
}
