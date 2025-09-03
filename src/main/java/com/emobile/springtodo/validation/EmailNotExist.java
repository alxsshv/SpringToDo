package com.emobile.springtodo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Проверка на наличие адреса электронной почты в записи другого пользователя */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailNotExistValidator.class)
public @interface EmailNotExist {

    /** Сообщение об ошибке  */
    String message() default "Email уже используется";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
