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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Сервис для работы с задачами
 * @author Aleksey Shvariov
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    /** Репозиторий для хранения задач в БД */
    private final TaskRepository taskRepository;

    /** Метод создания новой задачи.
     * @param task - номер задачи. */
    @Override
    @CacheEvict(value = "tasks")
    @Transactional
    public void createTask(Task task) {
        taskRepository.save(task);
    }

    /** Метод постраничного получения задач, размещённых на доске для задач.
     * @param boardId - уникальный идентификатор доски для задач, на которой размещены запрашиваемые задачи.
     * @param pageable - параметры пагинации в виде объекта {@link Pageable}
     * @return возвращает страницу с объектами {@link Task} */
    @Override
    public Page<Task> findAllTaskByBoardId(Long boardId, Pageable pageable) {
        return taskRepository.findByBoardId(boardId, pageable);
    }

    /** Метод поиска задачи по уникальному идентификатору.
     * @param taskId - уникальный идентификатор задачи.
     * @return возвращает экземпляр класса {@link Task}
     * @throws com.emobile.springtodo.exception.EntityNotFoundException будет выброшено, если задача не найдена.*/
    @Override
    @Cacheable(value = "tasks", key = "#taskId")
    public Task findTaskById(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new EntityNotFoundException("Задача не найдена по id = " + taskId);
        }
        return taskOpt.get();
    }

    /** Метод изменения сведений о задаче.
     * @param task - экземпляр класса {@link Task}, содержащий новые значения свойств задачи. */
    @Override
    @CacheEvict(value = "tasks", key = "#task.id")
    @Transactional
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

    /** Метод удаления задачи по уникальному идентификатору.
     * @param id  - уникальный идентификатор удаляемой задачи. */
    @Override
    @CacheEvict(value = "tasks", allEntries = true)
    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }
}
