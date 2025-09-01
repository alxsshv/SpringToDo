package com.emobile.springtodo.dto.mapper;

import com.emobile.springtodo.dto.BoardDto;
import com.emobile.springtodo.entity.Board;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Интерфейс для преобразования сущности {@link Board} в объект передачи данных и наоборот.
 * @author Shvariov Alexei
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BoardMapper {

    /** Метод преобразования доски для задач в объект передачи данных {@link BoardDto}
     * @param board - сущность доска для задач {@link Board}
     * @return возвращает объект передачи данных {@link BoardDto} */
    BoardDto map(Board board);

    /** Метод преобразования объекта передачи данных {@link BoardDto} в сущность {@link Board}
     * @param boardDto - объект передачи данных {@link BoardDto}
     * @return возвращает сущность доска для задач {@link Board}*/
    Board map(BoardDto boardDto);

    /** Метод преобразования списка досок для задач {@link Board} в список объектов передачи данных {@link BoardDto}
     * @param boards - коллекция (список) объектов {@link Board}.
     * @return возвращает список объектов передачи данных {@link BoardDto} */
    List<BoardDto> map(List<Board> boards);

}
