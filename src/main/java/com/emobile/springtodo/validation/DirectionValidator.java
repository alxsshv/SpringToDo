package com.emobile.springtodo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Валидатор для проверки правильности направления сортировки.
 * @author Aleksey Shvariov
 */

public class DirectionValidator implements ConstraintValidator<IsValidDirection, String> {

    /** Метод валидации введённого значения направления сортировки (по возрастанию, по убыванию)
     * @param value - строковое значение направления сортировки.
     * @return возвращает true, если значение валидно или false,
     * если значение value не является допустимым значение для направления сортировки.*/
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return (value.equalsIgnoreCase("ASC") || value.equalsIgnoreCase("DESC"));
    }
}
