package com.emobile.springtodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Объект передачи данных для класса {@link com.emobile.springtodo.entity.Board}
 * @author Aleksey Shavriov
 * */
@Schema(description = "Объект, описывающий доску для задач")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BoardDto {

    /** Идентификатор доски */
    @Schema(description = "Уникальный идентификатор доски для задач. " +
            "При создании задачи id не указывается",
            example = "1")
    private Long id;

    /** Пользователь (Владелец доски) */
    @Schema(description = "Уникальный идентификатор пользователя-владельца доски", example = "1")
    private Long userId;

    /** Название доски */
    @Schema(description = "Название доски для задача", example = "Рабочие задачи")
    @NotBlank(message = "Название доски для задач не может быть пустым")
    private String title;

}
