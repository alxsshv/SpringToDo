package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepository extends EntityRepository<Task, Long>{

    Page<Task> findByBoardId(Long boardId, Pageable pageable);
}
