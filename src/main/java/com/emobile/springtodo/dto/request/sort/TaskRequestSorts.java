package com.emobile.springtodo.dto.request.sort;

import lombok.Getter;

/** Перечисление, определяющие поля, пригодные для сортировки задач */
@Getter
public enum TaskRequestSorts {
    ID ("id"),
    TITLE ("title"),
    PRIORITY("task_priority"),
    STATUS("task_status"),
    COMPLETE_BEFORE("complete_before"),
    COMPLETE_DATE("complete_date"),
    BOARD_ID("board_id");

    private final String field;

    TaskRequestSorts(String field) {
        this.field = field;
    }
}
