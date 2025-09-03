package com.emobile.springtodo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Проверка на то, что данное свойство пользователя {@link com.emobile.springtodo.entity.ServiceUser}
 * может быть использовано при сортировке */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserSortPropertyValidator.class)
public @interface IsUserSortProperty {

    /** Сообщение об ошибке */
    String message() default "Сортировка по указанному полю не поддерживается";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
