package com.emobile.springtodo.validation;

import com.emobile.springtodo.dto.request.sort.UserRequestSorts;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSortPropertyValidator implements ConstraintValidator<IsUserSortProperty, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            UserRequestSorts.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
