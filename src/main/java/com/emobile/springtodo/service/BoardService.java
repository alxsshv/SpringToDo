package com.emobile.springtodo.service;

import com.emobile.springtodo.entity.Board;

import java.util.List;

/** Интерфейс, описывающий методы работы с досками для задач*/
public interface BoardService {

    /** Метод создания доски для задач, для определённого пользователя
     * @param board  - объект класса {@link Board}
     * @param userId - уникальный идентификатор пользователя, для которого создаётся задача */
    void createBoard(Board board, long userId);

    /** Метод изменения названия доски для задач.
     * @param board - объект класса {@link Board}, содержащий изменённое название доски для задач.
     * @return возвращает сохранённую после изменений версию доски для задач*/
    Board updateBoardTitle(Board board);

    /** Метод получения доски по уникальному идентификатору.
     * @param boardId - уникальный идентифкатор доски для задач.
     * @return возвращает доску для задач, если она найдена в базе данных.
     * @throws com.emobile.springtodo.exception.EntityNotFoundException будет выброшено
     * если доска не найдена в БД.*/
    Board findBoardById(Long boardId);

    /** Метод получения списка досок для задач принадлежащих пользователю
     *  с указанным в качестве параметра идентификатором.
     *  @param userId  - уникальный идентификатор пользователя.
     *  @return возвращает коллекцию (список) объектов класса {@link Board} */
    List<Board> findAllBoardsByUserId(Long userId);

    /** Метод удаления доски для записей по её уникальному идентификатору.
     * @param boardId - уникальный идентификатор доски для задач. */
    void deleteBoardById(Long boardId);


}
