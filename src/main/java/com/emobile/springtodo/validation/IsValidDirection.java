package com.emobile.springtodo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Aleksey Shvariov
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DirectionValidator.class)
public @interface IsValidDirection {
    String message() default "Неверно указано направление сортировки. Допустимые значения ASC или DESC";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
