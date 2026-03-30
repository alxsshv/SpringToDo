package com.emobile.springtodo.exception;

/** Исключение, возникающее в случае, если срок действия токена, указанного в запросе истёк. */
public class RefreshTokenExpirationException extends RuntimeException {

    /** Конструктор с возможностью передачи сообщения об ошибке.
     * @param message - строковое описание возможных причин возникновения исключения. */
    public RefreshTokenExpirationException(String message) {
        super(message);
    }
}
