package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.Priority;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.repository.impl.TaskRepositoryImpl;
import com.emobile.springtodo.repository.impl.mapper.TaskRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryImplTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    private static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17");

    @BeforeAll
    static void startDatabase() {
        POSTGRES.start();
    }


    @AfterAll
    static void stopDatabase() {
        POSTGRES.stop();
    }


    @Nested
    class FindByBoardIdWithPageableTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test findByBoardId with pageable when database not empty then return page of tasks")
        void testPageableFindByBoardId_whenDatabaseNotEmpty_thenReturnPageOfTasks() {
            TaskRepository taskRepository = new TaskRepositoryImpl(jdbcTemplate, new TaskRowMapper());
            final Long boardId = 1L;
            final int pageNum = 0;
            final int pageSize = 1;
            final int expectedTotalElements = 2;
            Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "complete_before"));
            Page<Task> tasksPage = taskRepository.findByBoardId(boardId, pageable);
            Assertions.assertEquals(pageSize, tasksPage.get().count());
            Assertions.assertEquals(pageSize, tasksPage.getContent().size());
            Assertions.assertEquals(expectedTotalElements, tasksPage.getTotalElements());
        }


        @Test
        @DisplayName("Test findByBoardId with pageable when database is empty then return empty page")
        void testPageableFindByBoardId_whenDatabaseIsEmpty_thenReturnEmptyPage() {
            TaskRepository taskRepository = new TaskRepositoryImpl(jdbcTemplate, new TaskRowMapper());
            final int pageNum = 0;
            final Long boardId = 1L;
            final int pageSize = 2;
            final int expectedTotalElements = 0;
            Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "complete_before"));
            Page<Task> tasksPage = taskRepository.findByBoardId(boardId, pageable);
            Assertions.assertTrue(tasksPage.isEmpty());
            Assertions.assertEquals(expectedTotalElements, tasksPage.getTotalElements());
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test findById when task is found then return optional of task")
        void testFindById_whenTaskIsFound_thenReturnOptionalOfTask() {
            final long taskId = 1L;
            TaskRepository taskRepository = new TaskRepositoryImpl(jdbcTemplate, new TaskRowMapper());
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            Assertions.assertTrue(taskOpt.isPresent());
            Assertions.assertEquals(taskId, taskOpt.get().getId());
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test findById when task not found then return empty optional")
        void testFindById_whenTaskIsNotFound_thenReturnEmptyOptional() {
            TaskRepository taskRepository = new TaskRepositoryImpl(jdbcTemplate, new TaskRowMapper());
            Optional<Task> taskOpt = taskRepository.findById(999L);
            Assertions.assertTrue(taskOpt.isEmpty());
        }
    }

    @Nested
    class SaveTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test save when add new task then return saved task entity")
        void testSave_whenAddNewTask_thenReturnSavedTask() {
            Task task = Task.builder()
                    .title("tests")
                    .body("write tests for repository")
                    .priority(Priority.MEDIUM)
                    .status(Status.IN_WAITING)
                    .boardId(1L)
                    .completeBefore(LocalDateTime.of(2025, Month.AUGUST, 11, 0,0, 0))
                    .completeDate(null)
                    .build();
            TaskRepository taskRepository = new TaskRepositoryImpl(jdbcTemplate, new TaskRowMapper());

            Task savedTask = taskRepository.save(task);

            Assertions.assertNotNull(savedTask);
            Assertions.assertNotNull(savedTask.getId());
            Assertions.assertEquals(task.getTitle(), savedTask.getTitle());
            Assertions.assertEquals(task.getBody(), savedTask.getBody());
            Assertions.assertEquals(task.getPriority(), savedTask.getPriority());
            Assertions.assertEquals(task.getStatus(), savedTask.getStatus());
            Assertions.assertEquals(task.getCompleteBefore(), savedTask.getCompleteBefore());
            Assertions.assertEquals(task.getCompleteDate(), savedTask.getCompleteDate());
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test save when update task data then return updated task")
        void testSave_whenUpdateTaskData_thenReturnUpdatedTask() {
            TaskRepository taskRepository = new TaskRepositoryImpl(jdbcTemplate, new TaskRowMapper());
            Task task = taskRepository.findById(2L).orElseThrow();
            task.setTitle("Купить слона");
            Task updatedTask = taskRepository.save(task);
            Assertions.assertNotNull(updatedTask);
            Assertions.assertEquals(task.getId(), updatedTask.getId());
            Assertions.assertEquals(task.getTitle(), updatedTask.getTitle());
            Assertions.assertNotNull(updatedTask.getBoardId());
        }
    }

    @Nested
    class DeleteMethodsTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test deleteAll when call method then table is cleared")
        void testDeleteALl_whenCallMethod_thenTableCleared() {
            final TaskRepository taskRepository = new TaskRepositoryImpl(jdbcTemplate, new TaskRowMapper());
            final long beforeCount = taskRepository.count();
            taskRepository.deleteAll();
            int countValueWhenEmpty = 0;
            Assertions.assertTrue(beforeCount > countValueWhenEmpty);
            Assertions.assertEquals(countValueWhenEmpty, taskRepository.count());
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test deleteById when call method then task is deleted")
        void testDeleteById_whenCallMethod_thenTaskIsDeleted() {
            final TaskRepository taskRepository = new TaskRepositoryImpl(jdbcTemplate, new TaskRowMapper());
            final long beforeCount = taskRepository.count();
            taskRepository.deleteById(2L);
            final long afterCount = taskRepository.count();
            Assertions.assertTrue(beforeCount > afterCount);
        }
    }

    @Test
    @Sql("test_data.sql")
    @DisplayName("Test count when table not empty then return valid value")
    void testCount_whenTableNotEmpty_thenReturnValidValue() {
        final long expectedCount = 3;
        final TaskRepository taskRepository = new TaskRepositoryImpl(jdbcTemplate, new TaskRowMapper());
        final long count = taskRepository.count();
        Assertions.assertEquals(expectedCount, count);
    }

}
