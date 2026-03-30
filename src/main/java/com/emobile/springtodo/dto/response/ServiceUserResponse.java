package com.emobile.springtodo.dto.response;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Set;

/** Объект передачи данных (запись) содержащий сведения об пользователе системы,
 *  возвращаемый в качестве ответа на запрос на получении информации о пользователе (пользователях)
 */
@Tag(name = "Сведения о пользователе")
public record ServiceUserResponse(@Parameter(name = "Уникальный идентификатор") Long id,
                                  @Parameter(name = "Имя пользователя (логин)") String username,
                                  @Parameter(name = "Адрес электронной почты") String email,
                                  @Parameter(name = "Список ролей пользователей") Set<String> roles) {}
