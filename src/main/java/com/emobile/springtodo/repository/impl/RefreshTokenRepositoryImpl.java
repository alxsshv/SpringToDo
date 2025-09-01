package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.entity.RefreshToken;
import com.emobile.springtodo.repository.RefreshTokenRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl extends EntityRepositoryImpl<RefreshToken, Long> implements RefreshTokenRepository {


    public RefreshTokenRepositoryImpl(JdbcTemplate jdbcTemplate, RowMapper<RefreshToken> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        final LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("token", token);
        return super.findByParameters(fields).stream().findFirst();
    }

    @Override
    public void deleteAllByUserId(long userId) {
        final String deleteQuery = "DELETE FROM " + getTableName() + " WHERE user_id = ?";
        jdbcTemplate.update(deleteQuery, userId);
    }

    @Override
    public void deleteAllByExpireDateLessThan(Instant dateTime) {
        final String deleteQuery = "DELETE FROM " + getTableName() + " WHERE expire_date < ?";
        jdbcTemplate.update(deleteQuery, Date.from(dateTime));
    }

    @Override
    public RefreshToken save(RefreshToken entity) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        if (entity.getId() != null) {
            fields.put("id", entity.getId());
        }
        fields.put("token", entity.getToken());
        fields.put("user_id", entity.getUserId());
        fields.put("expire_date", Timestamp.from(entity.getExpireDate()));
        return super.save(entity, fields);
    }

}
