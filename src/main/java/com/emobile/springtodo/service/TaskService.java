package com.emobile.springtodo.service;

import com.emobile.springtodo.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Интерфейс, описывающий методы работы с задачами. */
public interface TaskService {

    /** Метод создания новой задачи.
     * @param task - номер задачи. */
    void createTask(Task task);

    /** Метод постраничного получения задач, размещённых на доске для задач.
     * @param boardId - уникальный идентификатор доски для задач, на которой размещены запрашиваемые задачи.
     * @param pageable - параметры пагинации в виде объекта {@link Pageable}
     * @return возвращает страницу с объектами {@link Task} */
    Page<Task> findAllTaskByBoardId(Long boardId, Pageable pageable);

    /** Метод поиска задачи по уникальному идентификатору.
     * @param taskId - уникальный идентификатор задачи.
     * @return возвращает экземпляр класса {@link Task}
     * @throws com.emobile.springtodo.exception.EntityNotFoundException будет выброшено, если задача не найдена.*/
    Task findTaskById(Long taskId);

    /** Метод изменения сведений о задаче.
     * @param task - экземпляр класса {@link Task}, содержащий новые значения свойств задачи. */
    void updateTask(Task task);

    /** Метод удаления задачи по уникальному идентификатору.
     * @param id  - уникальный идентификатор удаляемой задачи. */
    void deleteById(Long id);

}
