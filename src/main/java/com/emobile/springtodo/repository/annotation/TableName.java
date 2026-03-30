package com.emobile.springtodo.repository.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Аннотация для указания имени таблицы, в которой хранятся соответствующие сущности.
 * Аннотация устанавливается на класс-сущность с указанием обязательного параметра - value.
 * Аннотация используется при выполнении
 * запросов к базе данных с использованием {@link com.emobile.springtodo.repository.EntityRepository}*/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {

    /** Имя таблицы, в которой хранятся записи о сущности */
    String value();
}
