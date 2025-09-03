package com.emobile.springtodo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Проверка на то, что данное свойство задачи {@link com.emobile.springtodo.entity.Task}
 * может быть использовано при сортировке */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaskSortPropertyValidator.class)
public @interface IsTaskSortProperty {

    /** Сообщение об ошибке */
    String message() default "Сортировка по указанному полю не поддерживается";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
