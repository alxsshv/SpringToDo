package com.emobile.springtodo.exception;

/** Исключение, выбрасываемое в случае отсутствия запрошенной информации в хранилище данных.*/
public class EntityNotFoundException extends RuntimeException {

    /** Конструктор с возможностью передачи сообщения об ошибке.
     * @param message - строковое описание возможных причин возникновения исключения. */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
