package com.emobile.springtodo.service.impl;

import com.emobile.springtodo.entity.Board;
import com.emobile.springtodo.exception.EntityNotFoundException;
import com.emobile.springtodo.repository.BoardRepository;
import com.emobile.springtodo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
/** Реализация сервиса для работы с досками для задач {@link Board} */
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    /** Репозиторий для работы с записями о досках для задач в БД */
    private final BoardRepository boardRepository;


    /** Метод создания доски для задач, для определённого пользователя
     * @param board  - объект класса {@link Board}
     * @param userId - уникальный идентификатор пользователя, для которого создаётся доска
     * @throws IllegalArgumentException если
     * доска с указанным названием уже создана для пользователя с указанным идентификатором.
     * */
    @Override
    @CacheEvict(value = "boards")
    @Transactional
    public void createBoard(Board board, long userId) {
        checkIfBoardAlreadyExists(board.getTitle(), userId);
        board.setUserId(userId);
        boardRepository.save(board);
    }
    /** Метод проверки наличия доски для задач с указанным именем у текущего пользователя.
     * @param boardTitle  - название доски для задач.
     * @param userId - уникальный идентификатор пользователя, для которого создаётся доска
     * @throws IllegalArgumentException если
     * доска с указанным названием уже создана для пользователя с указанным идентификатором.
     * */
    private void checkIfBoardAlreadyExists(String boardTitle, Long userId) {
        Optional<Board> boardOpt =  boardRepository.findByTitleAndUserId(boardTitle, userId);
        if (boardOpt.isPresent()) {
            throw new IllegalArgumentException("Доска для задач с названием "
                    + boardTitle + " уже существует");
        }
    }

    /** Метод получения списка досок для задач принадлежащих пользователю
     *  с указанным в качестве параметра идентификатором.
     *  @param userId  - уникальный идентификатор пользователя.
     *  @return возвращает коллекцию (список) объектов класса {@link Board} */
    @Override
    public List<Board> findAllBoardsByUserId(Long userId) {
        return boardRepository.findByUserId(userId);
    }

    /** Метод изменения названия доски для задач.
     * @param board - объект класса {@link Board}, содержащий изменённое название доски для задач.
     * @return возвращает сохранённую после изменений версию доски для задач*/
    @Override
    @CacheEvict(value = "boards", key = "#board.id")
    @Transactional
    public Board updateBoardTitle(Board board) {
        Board boardFromDb = findBoardById(board.getId());
        boardFromDb.setTitle(board.getTitle());
        return boardRepository.save(boardFromDb);
    }

    /** Метод получения доски по уникальному идентификатору.
     * @param boardId - уникальный идентифкатор доски для задач.
     * @return возвращает доску для задач, если она найдена в базе данных.
     * @throws com.emobile.springtodo.exception.EntityNotFoundException будет выброшено
     * если доска не найдена в БД.*/
    @Override
    @Cacheable(value = "boards", key = "#boardId")
    public Board findBoardById(Long boardId) {
        final Optional<Board> boardOpt = boardRepository.findById(boardId);
        return boardOpt.orElseThrow(() -> new EntityNotFoundException("Доска для задач не найдена по id = " + boardId));
    }

    /** Метод удаления доски для записей по её уникальному идентификатору.
     * @param boardId - уникальный идентификатор доски для задач. */
    @Override
    @CacheEvict(value = "boards", allEntries = true)
    public void deleteBoardById(Long boardId) {
        boardRepository.deleteById(boardId);
    }
}
