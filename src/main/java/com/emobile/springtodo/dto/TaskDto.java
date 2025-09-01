package com.emobile.springtodo.dto;

import com.emobile.springtodo.entity.Priority;
import com.emobile.springtodo.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Объект передачи данных для сущности {@link com.emobile.springtodo.entity.Task}
 * @author Aleksey Shvariov
 */

@Tag(name = "Сведения о задаче", description = "Объект передачи данных для задачи")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TaskDto {

    /** Идентификатор задачи */
    @Schema(description = "Идентификатор задачи (При создании новой задачи не указывается)", example = "1")
    private Long id;

    /** Название задачи */
    @Schema(description = "Название задачи", example = "Написать тесты")
    @NotBlank(message = "Не указан заголовок задачи")
    private String title;

    /** Содержание задачи */
    @Schema(description = "Содержание задачи",
            example = "Покрыть код тестами. Включая интеграционное и модульное тестирование")
    private String body;

    /** Приоритет (важность), по умолчанию = MEDIUM*/
    @Schema(description = "Приоритет (важность) задачи", example = "MEDIUM", allowableValues = {"LOW","MEDIUM", "HIGH"})
    private Priority priority;

    /** Состояние, по умолчанию = IN_WAITING */
    @Schema(description = "Текущий статус задачи",
            example = "IN_WAITING",
            allowableValues = {"IN_WAITING", "IN_PROGRESS", "COMPLETED"})
    private Status status;

    /** Срок выполнения */
    @Schema(description = "Планируемый срок (дата и время) выполнения задачи")
    private LocalDateTime completeBefore;

    /** Дата и время фактического выполнения */
    @Schema(description = "Фактическая дата и время выполнения задачи")
    private LocalDateTime completeDate;

    /** Идентификатор доски, к которой привязана данная задача */
    @Schema(description = "Идентификатор доски, на которой размещена задача")
    @NotNull(message = "Не указан идентификатор доски к которой привязана данная задача")
    @Min(value = 1, message = "Не корректное значение идентификатор доски")
    private Long boardId;

    /** Дата и время создания */
    @Schema(description = "Дата и время создания задачи")
    private LocalDateTime createDate;

    /** Дата и время обновления задачи */
    @Schema(description = "Дата и время обновления задачи")
    private LocalDateTime updateDate;

}
