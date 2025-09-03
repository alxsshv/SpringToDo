package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.entity.Board;
import com.emobile.springtodo.repository.BoardRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/** Реализация интерфейса {@link BoardRepository} */
@Repository
public class BoardRepositoryImpl extends EntityRepositoryImpl<Board, Long> implements BoardRepository {

    /** Конструктор с параметрами
     * @param jdbcTemplate - объект для взаимодействия с базой данных.
     * @param rowMapper - параметризованный интерфейс для сопоставления каждой строки {@link ResultSet}
     *                 с соответствующим объектом.
     */
    public BoardRepositoryImpl(JdbcTemplate jdbcTemplate, RowMapper<Board> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    /** Метод сохранения и обновления доски для задач в БД. Если переданный в качестве параметра
     *  объект не имеет идентификатора (id = null), создается новая запись в БД.
     *  Если переданный в качестве параметра объект имеет уникальный идентификатор,
     *  то обновляется уже существующая запись в БД.
     * @param board  - сохраняемый экземпляр класса {@link Board}
     * @return возвращает сохранённую (обновлённую) запись в БД в виде объекта {@link Board}*/
    @Override
    public Board save(Board board) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        if (board.getId() != null) {
            fields.put("id", board.getId());
        }
        fields.put("title", board.getTitle());
        fields.put("user_id", board.getUserId());
        return super.save(fields);
    }

    /** Метод получения списка досок для задач, идентификатор пользователя которых соответствует значению в параметре userId.
     *  @param userId - уникальный идентификатор пользователя, для которого должны быть найдены доски для задач
     *  @return возвращает список (коллекцию) досок для задач, у которых идентификатор пользователя равен указанному в параметре userId
     *  */
    @Override
    public List<Board> findByUserId(Long userId) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("user_id", userId);
        return findByParameters(parameters);
    }

    /**
     * Метод получения доски для задач по названию и идентификатору пользователя.
     * @param title - название доски для задач.
     * @param userId - уникальный идентификатор пользователя.
     * @return возвращает {@link Optional} с доской для задач, имеющей требуемое название и идентификатор пользователя,
     * или пустой Optional.
     * */
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
