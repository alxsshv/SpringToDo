package com.emobile.springtodo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaskSortPropertyValidator.class)
public @interface IsTaskSortProperty {
    String message() default "Сортировка по указанному полю не поддерживается";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
