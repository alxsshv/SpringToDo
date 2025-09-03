package com.emobile.springtodo.validation;

import com.emobile.springtodo.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Валидатор для проверки того, что адрес электронной почты не используется другим пользователем (не занят).  */
@Component
@RequiredArgsConstructor
public class EmailNotExistValidator implements ConstraintValidator<EmailNotExist, String> {

    /** Репозиторий для хранения пользователей в БД. */
    private final UserRepository userRepository;

    /** Метод проверки того, что адрес электронной почты не используется другим пользователем (не занят). */
    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return !userRepository.existByEmail(email);
    }
}
