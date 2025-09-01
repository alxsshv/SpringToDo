package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.entity.Board;
import com.emobile.springtodo.repository.BoardRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class BoardRepositoryImpl extends EntityRepositoryImpl<Board, Long> implements BoardRepository {


    public BoardRepositoryImpl(JdbcTemplate jdbcTemplate, RowMapper<Board> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    @Override
    public Board save(Board board) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        if (board.getId() != null) {
            fields.put("id", board.getId());
        }
        fields.put("title", board.getTitle());
        fields.put("user_id", board.getUserId());
        return super.save(board, fields);
    }

    @Override
    public List<Board> findByUserId(Long userId) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("user_id", userId);
        return findByParameters(parameters);
    }

    @Override
    public Optional<Board> findByTitleAndUserId(String title, Long userId) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("title", title);
        parameters.put("user_id", userId);
        List<Board> boards = findByParameters(parameters);
        if (boards.isEmpty()) {
            return Optional.empty();
        }
        if (boards.size() == 1) {
            return Optional.of(boards.getFirst());
        }
        throw new DataIntegrityViolationException("Количество найденных записей c userId ="
                + userId + " и title" + title);
    }
}
