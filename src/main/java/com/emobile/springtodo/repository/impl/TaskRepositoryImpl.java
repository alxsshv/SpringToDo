package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.repository.TaskRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@Repository
public class TaskRepositoryImpl extends EntityRepositoryImpl<Task, Long> implements TaskRepository {

    public TaskRepositoryImpl(JdbcTemplate jdbcTemplate, RowMapper<Task> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    @Override
    public Task save(Task entity) {
        validateEntity(entity);
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        if (entity.getId() != null) {
            fields.put("id", entity.getId());
        }
        fields.put("title", entity.getTitle());
        fields.put("body", entity.getBody());
        fields.put("task_status", entity.getStatus().name());
        fields.put("task_priority", entity.getPriority().name());
        fields.put("complete_before", entity.getCompleteBefore());
        fields.put("complete_date", entity.getCompleteDate());
        fields.put("board_id", entity.getBoardId());
        LocalDateTime createDate = entity.getCreateDate() != null ? entity.getCreateDate() : LocalDateTime.now();
        fields.put("create_date", createDate);
        fields.put("update_date", LocalDateTime.now());
        return super.save(entity, fields);
    }

    private void validateEntity(Task entity) {
        if (entity.getTitle() == null || entity.getTitle().isEmpty()) {
            throw new DataIntegrityViolationException("Содержание задачи не может быть пустым");
        }
        if (entity.getBoardId() == null) {
            throw new DataIntegrityViolationException("Идентификатор доски, к которой привязана задача не может быть null");
        }
    }


    @Override
    public Page<Task> findByBoardId(Long boardId, Pageable pageable) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("board_id", boardId);
        return super.findByParameters(fields, pageable);
    }

}
