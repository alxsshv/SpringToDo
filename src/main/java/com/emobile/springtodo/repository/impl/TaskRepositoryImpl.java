package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.repository.TaskRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

/**
 * Реализация интерфейса {@link TaskRepository}.
 * @author Aleksey Shvariov*
 */
@Repository
public class TaskRepositoryImpl extends EntityRepositoryImpl<Task, Long> implements TaskRepository {

    /** Конструктор с параметрами
     * @param jdbcTemplate - объект для взаимодействия с базой данных.
     * @param rowMapper - параметризованный интерфейс для сопоставления каждой строки {@link ResultSet}
     *                 с соответствующим объектом.
     */
    public TaskRepositoryImpl(JdbcTemplate jdbcTemplate, RowMapper<Task> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    /** Метод сохранения и обновления задач в БД. Если переданный в качестве параметра
     *  объект не имеет идентификатора (id = null), создается новая запись в БД.
     *  Если переданный в качестве параметра объект имеет уникальный идентификатор,
     *  то должна обновляться уже существующая запись в БД.
     * @param task  - сохраняемый экземпляр класса {@link Task}
     * @return возвращает сохранённую (обновлённую) запись в БД в виде объекта {@link Task} */
    @Override
    public Task save(Task task) {
        validateEntity(task);
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        if (task.getId() != null) {
            fields.put("id", task.getId());
        }
        fields.put("title", task.getTitle());
        fields.put("body", task.getBody());
        fields.put("task_status", task.getStatus().name());
        fields.put("task_priority", task.getPriority().name());
        fields.put("complete_before", task.getCompleteBefore());
        fields.put("complete_date", task.getCompleteDate());
        fields.put("board_id", task.getBoardId());
        LocalDateTime createDate = task.getCreateDate() != null ? task.getCreateDate() : LocalDateTime.now();
        fields.put("create_date", createDate);
        fields.put("update_date", LocalDateTime.now());
        return super.save(fields);
    }

    /** Метод проверки обязательных параметров задачи на null
     * @param task - объект класса {@link Task}, значения свойств которого необходимо проверить.
     * @throws DataIntegrityViolationException если, обязательные параметры задачи (title и boardId) равны null.*/
    private void validateEntity(Task task) {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new DataIntegrityViolationException("Содержание задачи не может быть пустым");
        }
        if (task.getBoardId() == null) {
            throw new DataIntegrityViolationException("Идентификатор доски, к которой привязана задача не может быть null");
        }
    }

    /** Метод постраничного получения всех задач, размещённых на доске для задач
     * с указанным в качестве параметра идентификатором.
     * @param boardId - уникальный идентификатор доски для задач.
     * @param pageable  - параметры возвращаемой страницы в виде объекта {@link Pageable}
     * @return возвращает страницу со списком задач, имеющих значение идентификатора доски,
     * соответствующее идентификатору, переданному в качестве параметра.
     * */
    @Override
    public Page<Task> findByBoardId(Long boardId, Pageable pageable) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("board_id", boardId);
        return super.findByParameters(fields, pageable);
    }

}
