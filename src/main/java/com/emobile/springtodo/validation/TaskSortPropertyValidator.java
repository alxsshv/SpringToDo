package com.emobile.springtodo.validation;

import com.emobile.springtodo.dto.request.sort.TaskRequestSorts;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Валидатор для проверки того, что данное свойство является валидным свойством
 * сущности {@link com.emobile.springtodo.entity.Task} и по нему можно делать сортировку.
 * @author Aleksey Shvariov
 */
public class TaskSortPropertyValidator implements ConstraintValidator<IsTaskSortProperty, String> {

    /** Метод проверки того, что данное свойство является валидным свойством
     * сущности {@link com.emobile.springtodo.entity.Task} и по нему можно делать сортировку.
     * @param value - название поля по которому планируется сортировать задачи.
     * @return - возвращает true, если поле можно использовать для сортировки,
     * false - если поле непригодно для сортировки
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            TaskRequestSorts.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
