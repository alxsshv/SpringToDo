package com.emobile.springtodo.repository.impl.mapper;

import com.emobile.springtodo.entity.ServiceUser;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor
@Component
public class ServiceUserRowMapper implements RowMapper<ServiceUser> {
    @Override
    public ServiceUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ServiceUser.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .build();
    }
}


