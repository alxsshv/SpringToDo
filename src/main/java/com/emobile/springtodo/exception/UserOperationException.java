package com.emobile.springtodo.exception;

/** Исключение, возникающее при нарушении пользователем прав доступа к данным.
 * Попытке получения доступа к данным другого пользователя*/
public class UserOperationException extends RuntimeException {

    /** Конструктор с возможностью передачи сообщения об ошибке.
     * @param message - строковое описание возможных причин возникновения исключения. */
    public UserOperationException(String message) {
        super(message);
    }
}
