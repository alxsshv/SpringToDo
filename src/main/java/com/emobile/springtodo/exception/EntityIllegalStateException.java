package com.emobile.springtodo.exception;

/** Исключение, выбрасываемое в случае несоблюдения правил построения репозиториев на основе {@link com.emobile.springtodo.repository.EntityRepository}. */
public class EntityIllegalStateException extends RuntimeException {

  /** Конструктор с возможностью передачи сообщения об ошибке.
   * @param message - строковое описание возможных причин возникновения исключения. */
  public EntityIllegalStateException(String message) {
    super(message);
  }
}
