package com.emobile.springtodo.dto.mapper;

import com.emobile.springtodo.dto.TaskDto;
import com.emobile.springtodo.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Интерфейс для преобразования сущности {@link Task} в объект передачи данных {@link TaskDto} и наоборот.
 * @author Shvariov Alexei
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    /** Метод преобразования сущности {@link Task} в объект передачи данных {@link TaskDto}
     * @param task - объект класса {@link Task}, который необходимо преобразовать.
     * @return возвращает объект передачи данных {@link TaskDto}.
     */
    TaskDto map(Task task);

    /** Метод преобразования DTO {@link TaskDto} в сущность {@link TaskDto}
     * @param taskDto - объект передачи данных {@link TaskDto}.
     * @return возвращает объект передачи данных {@link TaskDto}.
     */
    Task map(TaskDto taskDto);

}
