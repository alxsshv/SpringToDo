package com.emobile.springtodo.repository.impl.mapper;

import com.emobile.springtodo.entity.Board;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor
@Component
public class BoardRowMapper implements RowMapper<Board> {
    @Override
    public Board mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Board.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .userId(rs.getLong("user_id"))
                .build();
    }
}


