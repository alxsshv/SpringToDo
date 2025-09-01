package com.emobile.springtodo.dto.request.sort;

import lombok.Getter;

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
