package com.emobile.springtodo.dto.request.sort;

import lombok.Getter;

/** Перечисление, определяющие поля, пригодные для сортировки пользователей */
@Getter
public enum UserRequestSorts {
    ID ("id"),
    USERNAME ("username"),
    EMAIL("email"),
    ROLE ("security_role");

    private final String field;

    UserRequestSorts(String field) {
        this.field = field;
    }
}
