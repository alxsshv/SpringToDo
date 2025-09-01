package com.emobile.springtodo.repository.impl.mapper;

import com.emobile.springtodo.entity.Priority;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@NoArgsConstructor
@Component
public class TaskRowMapper implements RowMapper<Task> {

    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Task.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .body(rs.getString("body"))
                .priority(Priority.valueOf(rs.getString("task_priority")))
                .status(Status.valueOf(rs.getString("task_status")))
                .boardId(rs.getLong("board_id"))
                .completeBefore(dateTimeParse(rs.getTimestamp("complete_before")))
                .completeDate(dateTimeParse(rs.getTimestamp("complete_date")))
                .createDate(dateTimeParse(rs.getTimestamp("complete_date")))
                .updateDate(dateTimeParse(rs.getTimestamp("update_date")))
                .build();
    }

    private LocalDateTime dateTimeParse(Timestamp fieldValue) {
        if (fieldValue == null) {
            return null;
        }
        return fieldValue.toLocalDateTime();
    }
}


