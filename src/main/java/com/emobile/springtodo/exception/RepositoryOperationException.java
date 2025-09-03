package com.emobile.springtodo.exception;

/** Исключение возникающее в ходе взаимодействия репозиториев с БД.*/
public class RepositoryOperationException extends RuntimeException {

    /** Конструктор с возможностью передачи сообщения об ошибке.
     * @param message - строковое описание возможных причин возникновения исключения. */
    public RepositoryOperationException(String message) {
        super(message);
    }
}
