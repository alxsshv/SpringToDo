package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.Board;
import com.emobile.springtodo.repository.impl.BoardRepositoryImpl;
import com.emobile.springtodo.repository.impl.mapper.BoardRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BoardRepositoryImplTest {

        @Autowired
        private JdbcTemplate jdbcTemplate;

    private final static PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres");

        @DynamicPropertySource
        private static void setProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
                registry.add("spring.datasource.username", POSTGRES::getUsername);
                registry.add("spring.datasource.password", POSTGRES::getPassword);
                registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        }

        @BeforeAll
        public static void startDatabase() {
                POSTGRES.start();
        }


        @AfterAll
        public static void stopDatabase() {
                POSTGRES.stop();
        }



        @Nested
        class FindByUserIdTests {

            @Test
            @Sql({"test_data.sql"})
            @DisplayName("Test findByUserId when database not empty then return list of boards")
            public void testFindByUserId_whenDbNotEmpty_thenReturnListOfBoards() {
                int expectedBoardsSize = 2;
                BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                List<Board> boards = boardRepository.findByUserId(1L);
                Assertions.assertEquals(expectedBoardsSize, boards.size());
            }

            @Test
//            @Sql("test_schema.sql")
            @DisplayName("Test findByUserId when database is empty then return empty list")
            public void testFindAll_whenDbIsEmpty_thenReturnEmptyList() {
                BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                List<Board> boards = boardRepository.findByUserId(1L);
                Assertions.assertTrue(boards.isEmpty());
            }
        }

        @Nested
        class FindByTitleAndUserIdTests {

            @Test
            @Sql({"test_data.sql"})
            @DisplayName("Test findByTitleAndUserId when one match found then return optional of board")
            public void testFindByTitleAndUserId_whenOneMatchFound_thenReturnOptionalOfBoard() {
                String title = "home";
                Long userId = 1L;
                BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                Optional<Board> boardOpt = boardRepository.findByTitleAndUserId(title, userId);
                Assertions.assertTrue(boardOpt.isPresent());
                Assertions.assertEquals(title, boardOpt.get().getTitle());
                Assertions.assertEquals(userId, boardOpt.get().getUserId());
            }

            @Test
            @Sql({"test_data.sql"})
            @DisplayName("Test findByTitleAndUserId when no matches found then return empty optional")
            public void testFindByTitleAndUserId_whenNoMatchesFound_thenReturnEmptyOptional() {
                String title = "home";
                Long userId = 999L;
                BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                Optional<Board> boardOpt = boardRepository.findByTitleAndUserId(title, userId);
                Assertions.assertTrue(boardOpt.isEmpty());
            }

            @Test
            @Sql({"data_with_duplicate_board.sql"})
            @DisplayName("Test findByTitleAndUserId when more than one matches then return optional of boards")
            public void testFindByTitleAndUserId_whenMoreThanOneMatchesFound_thenThrowDataIntegrityViolationException() {
                String title = "work";
                Long userId = 1L;
                BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                Assertions.assertThrows(DataIntegrityViolationException.class,
                        () -> boardRepository.findByTitleAndUserId(title, userId));
            }

        }

        @Nested
        class FindByIdTests {

            @Test
            @Sql({"test_data.sql"})
            @DisplayName("Test findById when board is found then return optional of board")
            public void testFindById_whenBoardIsFound_thenReturnOptionalOfBoard() {
                final long boardId = 1L;
                BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                Optional<Board> boardOpt = boardRepository.findById(boardId);
                Assertions.assertTrue(boardOpt.isPresent());
                Assertions.assertEquals(boardId, boardOpt.get().getId());
            }

            @Test
            @Sql({"test_data.sql"})
            @DisplayName("Test findById when board is not found then return empty optional")
            public void testFindById_whenBoardIsNotFound_thenReturnEmptyOptional() {
                BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                Optional<Board> boardOpt = boardRepository.findById(999L);
                Assertions.assertTrue(boardOpt.isEmpty());
            }
        }

        @Nested
        class SaveTests {

            @Test
            @Sql({"test_data.sql"})
            @DisplayName("Test save when add new board then return saved board entity")
            public void testSave_whenAddNewBoard_thenReturnSavedBoard() {
                Board board = Board.builder()
                        .title("testboard")
                        .userId(1L)
                        .build();
                BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                Board savedBoard = boardRepository.save(board);
                Assertions.assertNotNull(savedBoard);
                Assertions.assertNotNull(savedBoard.getId());
                Assertions.assertEquals(board.getTitle(), savedBoard.getTitle());
                Assertions.assertEquals(board.getUserId(), savedBoard.getUserId());
            }

            @Test
            @Sql({"test_data.sql"})
            @DisplayName("Test save when update board data then return updated board")
            public void testSave_whenUpdateBoardData_thenReturnUpdatedBoard() {
                String title = "work";
                Long userId = 1L;
                BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                Board board = boardRepository.findByTitleAndUserId(title, userId).orElseThrow();
                board.setTitle("homework");

                Board updatedBoard = boardRepository.save(board);

                Assertions.assertNotNull(updatedBoard);
                Assertions.assertEquals(board.getId(), updatedBoard.getId());
                Assertions.assertEquals(board.getTitle(), updatedBoard.getTitle());
                Assertions.assertNotNull(updatedBoard.getUserId());
            }
        }

        @Nested
        class DeleteMethodsTests {

            @Test
            @Sql({"test_data.sql"})
            @DisplayName("Test deleteAll when call method then table is cleared")
            public void testDeleteALl_whenCallMethod_thenTableCleared() {
                final BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                final long beforeCount = boardRepository.count();
                boardRepository.deleteAll();
                int countValueWhenEmpty = 0;
                Assertions.assertTrue(beforeCount > countValueWhenEmpty);
                Assertions.assertEquals(countValueWhenEmpty, boardRepository.count());
            }

            @Test
            @Sql({"test_data.sql"})
            @DisplayName("Test deleteById when call method then board is deleted")
            public void testDeleteById_whenCallMethod_thenBoardIsDeleted() {
                final BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
                final long beforeCount = boardRepository.count();
                boardRepository.deleteById(2L);
                final long afterCount = boardRepository.count();
                Assertions.assertTrue(beforeCount > afterCount);
            }
        }

    @Test
    @Sql({"test_data.sql"})
    @DisplayName("Test count when table not empty then return valid value")
    public void testCount_whenTableNotEmpty_thenReturnValidValue() {
        final long expectedCount = 3;
        final BoardRepository boardRepository = new BoardRepositoryImpl(jdbcTemplate, new BoardRowMapper());
        final long count = boardRepository.count();
        Assertions.assertEquals(expectedCount, count);
    }

}
