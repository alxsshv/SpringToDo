package com.emobile.springtodo.service;

import com.emobile.springtodo.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    void createTask(Task task);

    Page<Task> findAllTaskByBoardId(Long boardId, Pageable pageable);

    Task findTaskById(Long taskId);

    void updateTask(Task Task);

    void deleteById(Long id);

}
