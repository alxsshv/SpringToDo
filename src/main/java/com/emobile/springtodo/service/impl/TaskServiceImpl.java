package com.emobile.springtodo.service.impl;

import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.exception.EntityNotFoundException;
import com.emobile.springtodo.repository.TaskRepository;
import com.emobile.springtodo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Aleksey Shvariov
 */

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;


    @Override
    @CacheEvict(value = "tasks")
    public void createTask(Task task) {
        taskRepository.save(task);
    }

    @Override
    public Page<Task> findAllTaskByBoardId(Long boardId, Pageable pageable) {
        return taskRepository.findByBoardId(boardId, pageable);
    }

    @Override
    @Cacheable(value = "tasks", key = "#taskId")
    public Task findTaskById(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new EntityNotFoundException("Задача не найдена по id = " + taskId);
        }
        return taskOpt.get();
    }

    @Override
    @CacheEvict(value = "tasks", key = "#task.id")
    public void updateTask(Task task) {
        Task taskFromDb = findTaskById(task.getId());
        taskFromDb.setTitle(task.getTitle());
        taskFromDb.setBody(task.getBody());
        taskFromDb.setPriority(task.getPriority());
        taskFromDb.setStatus(task.getStatus());
        taskFromDb.setCompleteBefore(task.getCompleteBefore());
        taskFromDb.setCompleteDate(task.getCompleteDate());
        taskRepository.save(taskFromDb);
    }

    @Override
    @CacheEvict(value = "tasks", allEntries = true)
    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }
}
