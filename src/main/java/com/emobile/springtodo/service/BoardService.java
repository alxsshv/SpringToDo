package com.emobile.springtodo.service;

import com.emobile.springtodo.entity.Board;

import java.util.List;

public interface BoardService {

    void createBoard(Board board, long userId);

    Board updateBoardTitle(Board board);

    Board findBoardById(Long boardId);

    List<Board> findAllBoardsByUserId(Long userId);

    void deleteBoardById(Long boardId);


}
