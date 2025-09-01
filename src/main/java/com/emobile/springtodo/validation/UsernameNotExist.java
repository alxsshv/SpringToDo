package com.emobile.springtodo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameNotExistValidator.class)
public @interface UsernameNotExist {

    String message() default "Пользователь с таким именем пользователя уже зарегистрирован в сервисе";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
