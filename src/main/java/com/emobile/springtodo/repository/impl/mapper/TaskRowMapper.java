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

/** Класс, реализующий метод для сопоставления каждой строки результирующего набора {@link ResultSet} с
 *  соответствующим объектом класса {@link Task}
 */
@NoArgsConstructor
@Component
public class TaskRowMapper implements RowMapper<Task> {


    /** Метод для сопоставления каждой строки результирующего набора {@link ResultSet} с
     *  соответствующим объектом класса {@link Task} */
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

    /** Метод парсинга объекта возвращаемого БД объекта Timestamp в {@link LocalDateTime}
     * @param fieldValue - значение Timestamp полученное в результирующем наборе.
     * @return возвращает значение {@link LocalDateTime} соответствующее значению
     * Timestamp передаваемого в метод в качестве параметра, если параметра равно null, метод возвращает null */
    private LocalDateTime dateTimeParse(Timestamp fieldValue) {
        if (fieldValue == null) {
            return null;
        }
        return fieldValue.toLocalDateTime();
    }
}


