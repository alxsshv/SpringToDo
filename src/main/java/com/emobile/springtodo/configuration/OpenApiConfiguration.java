package com.emobile.springtodo.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Конфигурация OpenApi.
 * @author Aleksey Shvariov
 */


@OpenAPIDefinition(
        info = @Info(
                title = "SpringToDo ",
                description = "Приложение для планирования и отслеживания выполнения задач. " +
                        "Каждый пользователь может создавать доски для задач и размещать на них задачи. " +
                        "Задачи имеют статусы и приоритеты",
                version = "1.0.0"
        )
)

@SecurityScheme(name = "JWT", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class OpenApiConfiguration {
}
