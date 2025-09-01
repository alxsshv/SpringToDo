package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.Board;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends EntityRepository<Board, Long>{

    List<Board> findByUserId(Long userId);

    Optional<Board> findByTitleAndUserId(String title, Long userId);
}
