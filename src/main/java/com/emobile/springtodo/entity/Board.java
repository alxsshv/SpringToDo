package com.emobile.springtodo.entity;

import com.emobile.springtodo.repository.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 * Доска с заданиями.
 * У пользователя может быть несколько досок, например домашние дела, рабочие дела
 * @author Alexei Shvariov
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@TableName("boards")
public class Board implements Identifiable<Long>, Serializable {

    /** Идентификатор доски */
    private Long id;
    /** Пользователь (Владелец доски) */
    private Long userId;
    /** Название доски */
    private String title;


}
