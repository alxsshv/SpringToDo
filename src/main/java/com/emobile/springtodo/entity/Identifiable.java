package com.emobile.springtodo.entity;

/** Интерфейс, гарантирующий, что все его реализации будут содержать уникальный идентификатор */
public interface Identifiable<ID> {

    /**Метод возвращающий значение уникального идентификатора сущности*/
    ID getId();
}
