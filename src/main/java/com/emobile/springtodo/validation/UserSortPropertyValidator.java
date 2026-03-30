package com.emobile.springtodo.validation;

import com.emobile.springtodo.dto.request.sort.UserRequestSorts;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Валидатор для проверки того, что указанное свойство является свойством
 * объекта {@link com.emobile.springtodo.entity.ServiceUser} и по нему можно выполнять сортировку*/
@Component
@RequiredArgsConstructor
public class UserSortPropertyValidator implements ConstraintValidator<IsUserSortProperty, String> {

    /** Метод для проверки того, что указанное свойство является свойством
     * объекта {@link com.emobile.springtodo.entity.ServiceUser} и по нему можно выполнять сортировку
     * @param value - название поля по которому планируется сортировать пользователей.
     * @return - возвращает true, если поле можно использовать для сортировки,
     * false - если поле непригодно для сортировки*/
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
