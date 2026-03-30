package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.entity.RefreshToken;
import com.emobile.springtodo.repository.RefreshTokenRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Реализация интерфейса {@link RefreshTokenRepository}.
 * @author Aleksey Shvariov*
 */

@Repository
public class RefreshTokenRepositoryImpl extends EntityRepositoryImpl<RefreshToken, Long> implements RefreshTokenRepository {

    /** Конструктор с параметрами
     * @param jdbcTemplate - объект для взаимодействия с базой данных.
     * @param rowMapper - параметризованный интерфейс для сопоставления каждой строки {@link ResultSet}
     *                 с соответствующим объектом.
     */
    public RefreshTokenRepositoryImpl(JdbcTemplate jdbcTemplate, RowMapper<RefreshToken> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    /**
     * Метод получения данных о refresh-токене по токену.
     * @param token - значение токена для поиска записи об объекте {@link RefreshToken}.
     * @return возвращает {@link Optional} с объектом класса {@link RefreshToken},
     * или пустой Optional если запись об объекте не найдена.
     */
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        final LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("token", token);
        return super.findByParameters(fields).stream().findFirst();
    }

    /** Удаление всех записей о refresh-токенах пользователя с указанным идентификатором.
     * @param userId - уникальный идентификатор пользователя.*/
    @Override
    public void deleteAllByUserId(long userId) {
        final String deleteQuery = "DELETE FROM " + getTableName() + " WHERE user_id = ?";
        jdbcTemplate.update(deleteQuery, userId);
    }

    /** Удаление всех записей о refresh-токенах срок действия которых (expireDate) меньше указанного значения.
     * @param dateTime  - временная граница, для удаления токенов срок действия которых закончился ранее установленной границы.
     */
    @Override
    public void deleteAllByExpireDateLessThan(Instant dateTime) {
        final String deleteQuery = "DELETE FROM " + getTableName() + " WHERE expire_date < ?";
        jdbcTemplate.update(deleteQuery, Date.from(dateTime));
    }

    /** Метод сохранения и обновления токена в БД. Если переданный в качестве параметра
     *  объект не имеет идентификатора (id = null), создается новая запись в БД.
     *  Если переданный в качестве параметра объект имеет уникальный идентификатор,
     *  то обновляется уже существующая запись в БД.
     * @param refreshToken  - сохраняемый экземпляр класса {@link RefreshToken}
     * @return возвращает сохранённую (обновлённую) запись в БД в виде объекта {@link RefreshToken}*/
    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        if (refreshToken.getId() != null) {
            fields.put("id", refreshToken.getId());
        }
        fields.put("token", refreshToken.getToken());
        fields.put("user_id", refreshToken.getUserId());
        fields.put("expire_date", Timestamp.from(refreshToken.getExpireDate()));
        return super.save(fields);
    }

}
