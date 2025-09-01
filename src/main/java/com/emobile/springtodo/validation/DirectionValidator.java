package com.emobile.springtodo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Aleksey Shvariov
 */

public class DirectionValidator implements ConstraintValidator<IsValidDirection, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return (value.equalsIgnoreCase("ASC") || value.equalsIgnoreCase("DESC"));
    }
}
