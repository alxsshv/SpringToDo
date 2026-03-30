package com.emobile.springtodo.exception;

/** Исключение, возникающее при несоответствии данных, указанных в запросе требованиям валидации. */
public class IllegalRequestArgumentsException extends RuntimeException {

    /** Конструктор с возможностью передачи сообщения об ошибке.
     * @param message - строковое описание возможных причин возникновения исключения. */
    public IllegalRequestArgumentsException(String message) {
        super(message);
    }
}
