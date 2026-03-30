package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Интерфейс для реализации репозитория, обеспечивающего выполнение CRUD-операций с сущностью {@link  Task} */
public interface TaskRepository extends EntityRepository<Task, Long>{

    /** Метод сохранения и обновления задач в БД. Если переданный в качестве параметра
     *  объект не имеет идентификатора (id = null), создается новая запись в БД.
     *  Если переданный в качестве параметра объект имеет уникальный идентификатор,
     *  то должна обновляться уже существующая запись в БД.
     * @param task  - сохраняемый экземпляр класса {@link Task}
     * @return возвращает сохранённую (обновлённую) запись в БД в виде объекта {@link Task} */
    Task save(Task task);

    /** Метод постраничного получения всех задач, размещённых на доске для задач
     * с указанным в качестве параметра идентификатором.
     * @param boardId - уникальный идентификатор доски для задач.
     * @param pageable  - параметры возвращаемой страницы в виде объекта {@link Pageable}
     * @return возвращает страницу со списком задач, имеющих значение идентификатора доски,
     * соответствующее идентификатору, переданному в качестве параметра.
     * */
    Page<Task> findByBoardId(Long boardId, Pageable pageable);
}
