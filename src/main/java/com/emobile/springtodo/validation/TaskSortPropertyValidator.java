package com.emobile.springtodo.validation;

import com.emobile.springtodo.dto.request.sort.TaskRequestSorts;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Aleksey Shvariov
 */

public class TaskSortPropertyValidator implements ConstraintValidator<IsTaskSortProperty, String> {

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
