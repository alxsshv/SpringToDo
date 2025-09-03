package com.emobile.springtodo.repository.impl.mapper;

import com.emobile.springtodo.entity.RefreshToken;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  Класс, реализующий метод для сопоставления каждой строки результирующего набора {@link ResultSet} с
 *  соответствующим объектом класса {@link RefreshToken}
 * @author Aleksey Shvariov
 */

@Component
public class RefreshTokenRowMapper implements RowMapper<RefreshToken> {

    /** Метод для сопоставления каждой строки результирующего набора {@link ResultSet} с
     *  соответствующим объектом класса {@link RefreshToken} */
    @Override
    public RefreshToken mapRow(ResultSet rs, int rowNum) throws SQLException {
        return RefreshToken.builder()
                .id(rs.getLong("id"))
                .token(rs.getString("token"))
                .userId(rs.getLong("user_id"))
                .expireDate((rs.getTimestamp("expire_date").toInstant()))
                .build();
    }
}
