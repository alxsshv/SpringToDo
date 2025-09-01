package com.emobile.springtodo.service.impl;

import com.emobile.springtodo.entity.Board;
import com.emobile.springtodo.exception.EntityNotFoundException;
import com.emobile.springtodo.repository.BoardRepository;
import com.emobile.springtodo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;


    @Override
    @CacheEvict(value = "boards")
    public void createBoard(Board board, long userId) {
        checkIfBoardAlreadyExists(board.getTitle(), userId);
        board.setUserId(userId);
        boardRepository.save(board);
    }

    private void checkIfBoardAlreadyExists(String boardTitle, Long userId) {
        Optional<Board> boardOpt =  boardRepository.findByTitleAndUserId(boardTitle, userId);
        if (boardOpt.isPresent()) {
            throw new IllegalArgumentException("Доска для задач с названием "
                    + boardTitle + " уже существует");
        }
    }

    @Override
    public List<Board> findAllBoardsByUserId(Long userId) {
        return boardRepository.findByUserId(userId);
    }

    @Override
    @CacheEvict(value = "boards", key = "#board.id")
    public Board updateBoardTitle(Board board) {
        Board boardFromDb = findBoardById(board.getId());
        boardFromDb.setTitle(board.getTitle());
        return boardRepository.save(boardFromDb);
    }

    @Override
    @Cacheable(value = "boards", key = "#boardId")
    public Board findBoardById(Long boardId) {
        Optional<Board> boardOpt = boardRepository.findById(boardId);
        return boardOpt.orElseThrow(() -> new EntityNotFoundException("Доска для задач не найдена по id = " + boardId));
    }

    @Override
    @CacheEvict(value = "boards", allEntries = true)
    public void deleteBoardById(Long boardId) {
        boardRepository.deleteById(boardId);
    }
}
