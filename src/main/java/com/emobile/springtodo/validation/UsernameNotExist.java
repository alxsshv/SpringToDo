package com.emobile.springtodo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Аннотация для проверки того, что данное имя пользователя ранее не использовано другим пользователем (не занято) */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameNotExistValidator.class)
public @interface UsernameNotExist {

    /** Сообщение об ошибке */
    String message() default "Пользователь с таким именем пользователя уже зарегистрирован в сервисе";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
