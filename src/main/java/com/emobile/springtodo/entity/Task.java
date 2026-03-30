package com.emobile.springtodo.entity;

import com.emobile.springtodo.repository.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Запланированное задание
 * @author Alexei Shvariov
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@TableName("tasks")
public class Task implements Identifiable<Long>, Serializable {

    /** Идентификатор задачи */
    private Long id;

    /** Название задачи */
    private String title;

    /** Описание задачи */
    private String body;

    /** Приоритет (важность) */
    private Priority priority;

    /** Состояние */
    private Status status;

    /** Срок выполнения */
    private LocalDateTime completeBefore;

    /** Дата фактического выполнения */
    private LocalDateTime completeDate;

    /** Идентификатор доски, к которой привязана данная задача */
    private Long boardId;

    /** Дата создания */
    private LocalDateTime createDate;

    /** Дата последнего обновления */
    private LocalDateTime updateDate;
}
