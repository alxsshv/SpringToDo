package com.emobile.springtodo.exception;

import org.springframework.security.core.AuthenticationException;

/** Исключение, выбрасываемое в случае возникновения ошибок аутентификации пользователя.
 * Например, если пользователь не найден по имени пользователя
 * и адресу электронной почты в ходе базовой аутентификации.*/
public class ServiceAuthenticationException extends AuthenticationException {

    /** Конструктор с возможностью передачи сообщения об ошибке.
     * @param message - строковое описание возможных причин возникновения исключения. */
    public ServiceAuthenticationException(String message) {
        super(message);
    }
}
