package com.emobile.springtodo.entity;

import com.emobile.springtodo.repository.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Сущность, хранящая информацию о refresh-токенах */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("refresh_tokens")
public class RefreshToken implements Identifiable<Long>{

    /** Уникальный идентификатор */
    private Long id;

    /** Идентификатор пользователя */
    private Long userId;

    /** Токен */
    private String token;

    /** Срок действия токена */
    private Instant expireDate;

}


