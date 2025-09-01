package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.BoardDto;
import com.emobile.springtodo.dto.request.LoginRequest;
import com.emobile.springtodo.entity.Board;
import com.emobile.springtodo.repository.BoardRepository;
import com.emobile.springtodo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Aleksey Shvariov
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BoardControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.5");

    @Container
    private static final RedisContainer REDIS
            = new RedisContainer(DockerImageName.parse("redis:8.0.3")).withExposedPorts(6379);

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", () -> REDIS.getMappedPort(6379).toString());
    }

    @BeforeAll
    public static void startDatabase() {
        POSTGRES.start();
        REDIS.start();
    }

    @AfterAll
    public static void stopDatabase() {
        POSTGRES.stop();
        REDIS.stop();
    }

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    public void clearDatabase() {
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    public RequestPostProcessor signInWithUser(String username, String password) {
        return request -> {
            try {
                request.addHeader("Authorization", "Bearer " + getAccessTokenForUser(username, password));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return request;
        };
    }

    private String getAccessTokenForUser(String username, String password) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andReturn();
        JSONObject response = new JSONObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String accessToken = response.getString("accessToken");
        Assertions.assertNotNull(accessToken, "В ходе авторизации не получен JwtAccessToken");
        return accessToken;
    }


    @Nested
    class CreateBoardTests {
        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createBoard when send valid request then board created")
        public void testCreateBoard_whenSendValidRequest_thenBoardCreated() throws Exception {
            final BoardDto boardDto = BoardDto.builder()
                    .title("NewBoardForTasks")
                    .userId(2L)
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.CREATED.value());
            expected.put("message", "Добавлена доска для задач " + boardDto.getTitle());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(boardDto))
                            .with(signInWithUser("user2", "pass2")))
                    .andExpect(status().is(HttpStatus.CREATED.value()))
                    .andReturn();
            System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createBoard when userId not matched with authentication principal id return created status and" +
                " board has been created with authentication principal id")
        public void testCreateBoard_whenUserIdNotMatchedByAuthenticationPrincipal_thenReturnCreated() throws Exception {
            final long wrongUserId = 1L;
            final long idForUser2 = 2L;
            final BoardDto boardDto = BoardDto.builder()
                    .title("NewBoardForTasksByUser2")
                    .userId(wrongUserId)
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.CREATED.value());
            expected.put("message", "Добавлена доска для задач " + boardDto.getTitle());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(boardDto))
                            .with(signInWithUser("user2", "pass2")))
                    .andExpect(status().is(HttpStatus.CREATED.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Optional<Board> boardOpt = boardRepository.findByTitleAndUserId(boardDto.getTitle(), idForUser2);
            Assertions.assertTrue(boardOpt.isPresent());

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createBoard when userId is null then return created status and board has been created correctly")
        public void testCreateBoard_whenUserIdIsNull_thenReturnCreated() throws Exception {
            final BoardDto boardDto = BoardDto.builder()
                    .title("NewBoardForTasks")
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.CREATED.value());
            expected.put("message", "Добавлена доска для задач " + boardDto.getTitle());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(boardDto))
                            .with(signInWithUser("user2", "pass2")))
                    .andExpect(status().is(HttpStatus.CREATED.value()))
                    .andReturn();

            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }


        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createBoard when title is empty then return bad request status")
        public void testCreateBoard_whenTitleIsEmpty_thenReturnBadRequest() throws Exception {
            final BoardDto boardDto = BoardDto.builder()
                    .title("")
                    .userId(2L)
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(boardDto))
                            .with(signInWithUser("user2", "pass2")))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }
    }

    @Nested
    class GetAllByUserTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllByUser when send valid request then return list of BoardDto")
        public void testGetAllByUser_whenSendValidRequest_thenReturnListOfBoards() throws Exception {

            JSONArray expected = new JSONArray("[" +
                    "{\"id\":1,\"userId\":1,\"title\":\"work\"}," +
                    "{\"id\":2,\"userId\":1,\"title\":\"home\"}" +
                    "]");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/boards")
                            .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andReturn();

            JSONArray actual = new JSONArray(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }
    }

    @Nested
    class GetBoardByIdTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getBoardById when board is found then return boardDto")
        public void testGetBoardById_whenBoardIsFound_thenReturnBoardDto() throws Exception {
            final long boardId = 2L;
            String expected = "{\"id\":2, \"userId\":1, \"title\":\"home\"}";

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/boards/" + boardId)
                    .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andReturn();
            String actual = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getBoardById when board is not found then return not found status")
        public void testGetBoardById_whenBoardNotFound_thenReturn404() throws Exception {
            final long boardId = 5L;
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.NOT_FOUND.value());
            expected.put("message", "Объект не найден");

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/boards/" + boardId)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getBoardById when requested board does not belong to authentication principal" +
                " then return bad request status")
        public void testGetBoardById_whenBoardDoesNotBelongToUser_thenReturnBadRequest() throws Exception {
            final long boardId = 1L;
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.FORBIDDEN.value());

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/boards/" + boardId)
                            .with(signInWithUser("user2", "pass2")))
                    .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

    }

    @Nested
    class UpdateBoard {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test updateBoard when send valid request then board has been updated success")
        public void testUpdateBoard_whenSendValidRequest_thenBoardSuccessUpdated() throws Exception {
            final BoardDto boardDto = BoardDto.builder()
                    .id(2L)
                    .title("HomeTasks")
                    .userId(1L)
                    .build();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.OK.value());
            expected.put("message", "Название доски для задач изменено на " + boardDto.getTitle());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(boardDto))
                            .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            Optional<Board> boardOpt = boardRepository.findByTitleAndUserId(boardDto.getTitle(), boardDto.getUserId());

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            Assertions.assertTrue(boardOpt.isPresent());
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test updateBoard when user is not the owner of the updated board then return forbidden status")
        public void testUpdateBoard_whenUserIsNotOwnerOfUpdatedBoard_thenReturnForbidden() throws Exception {
            final BoardDto boardDto = BoardDto.builder()
                    .id(2L)
                    .title("HomeTasks")
                    .userId(1L)
                    .build();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.FORBIDDEN.value());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/boards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(boardDto))
                            .with(signInWithUser("user2", "pass2")))
                    .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }
    }

    @Nested
    class DeleteByIdTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test deleteById when send valid request then board has been deleted")
        public void testDeleteById_whenSendValidRequest_thenBoardDeleted() throws Exception {
            final long boardId = 2L;
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.NO_CONTENT.value());
            expected.put("message", "Доска для задач c id = "+ boardId +" успешно удалена");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/boards/" + boardId)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.NO_CONTENT.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test deleteById when board is not found then return not found status")
        public void testDeleteById_whenBoardIsNotFound_thenReturnNotFoundStatus() throws Exception {
            final long boardId = 999L;
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.NOT_FOUND.value());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/boards/" + boardId)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test deleteById user is not the owner of the board then return forbidden status")
        public void testDeleteById_whenUserIsNotOwnerOfBoard_thenReturnForbidden() throws Exception {
            final long boardId = 2L;
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.FORBIDDEN.value());
            expected.put("message", "Недостаточно прав для удаления доски");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/boards/" + boardId)
                    .with(signInWithUser("user2", "pass2")))
                    .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

        }

    }









}
