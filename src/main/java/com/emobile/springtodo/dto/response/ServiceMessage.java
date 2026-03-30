package com.emobile.springtodo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Сервисное сообщение, предназначенное для передачи клиенту информации об успешном выполнении запроса
 * или возникновении ошибки.
 * @author Aleksey Shvariov
 */
@Schema(description = "Сервисное сообщение об успешном выполнении операции или возникновении ошибки")
@AllArgsConstructor
@Getter
@Setter
public class ServiceMessage {

    /** Числовое значение кода ответа сервера */
    @Schema(description = "Числовое значение кода ответа сервера", example = "200")
    private int statusCode;

    /** Текстовое сообщение об успешном выполнении запроса или сведения об ошибке. */
    @Schema(description = "Текстовое сообщение", example = "Добавлена доска для задач")
    private String message;
}
