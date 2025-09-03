package com.emobile.springtodo.validation;

import com.emobile.springtodo.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/** Валидатор для проверки того, что указанное имя пользователя не занято другим пользователем */
@Component
@RequiredArgsConstructor
public class UsernameNotExistValidator implements ConstraintValidator<UsernameNotExist, String> {

    /** Репозиторий для хранения пользователей */
    private final UserRepository userRepository;

    /** Метод проверки того, что имя пользователя не используется другим пользователем (не занято).
     * @param username - имя пользователя.
     * @return возвращает true если имя пользователя не занято, false - если данное имя пользователя уже используется*/
    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
        return !userRepository.existByUsername(username);
    }
}
