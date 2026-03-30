package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.TaskDto;
import com.emobile.springtodo.dto.mapper.TaskMapper;
import com.emobile.springtodo.dto.request.LoginRequest;
import com.emobile.springtodo.entity.Priority;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    private static final PostgreSQLContainer<?> POSTGRES
            = new PostgreSQLContainer<>("postgres:17.5");

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

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    public void clearDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "tasks", "boards", "service_users");
    }

    @AfterAll
    public static void stopDatabase() {
        POSTGRES.stop();
        REDIS.stop();
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
    class CreateTaskTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createTask when send valid request then return created status")
        public void testCreateTask_whenSendValidRequest_thenReturnCreated() throws Exception {
            TaskDto taskDto = TaskDto.builder()
                    .title("Add tests")
                    .body("Add tests for TaskController class")
                    .boardId(1L)
                    .status(Status.IN_WAITING)
                    .priority(Priority.MEDIUM)
                    .completeBefore(LocalDateTime.now().plusDays(2))
                    .build();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.CREATED.value());
            expected.put("message", "Создана задача " + taskDto.getTitle());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(taskDto))
                    .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.CREATED.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createTask when user is not the owner of the board where the task is added then return forbidden status")
        public void testCreateTask_whenUserCannotAddTaskToSpecifiedBoard_thenReturnForbidden() throws Exception {
            TaskDto taskDto = TaskDto.builder()
                    .title("Add tests")
                    .body("Add tests for TaskController class")
                    .boardId(1L)
                    .status(Status.IN_WAITING)
                    .priority(Priority.MEDIUM)
                    .completeBefore(LocalDateTime.now().plusDays(2))
                    .build();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.FORBIDDEN.value());
            expected.put("message", "Доска для задач с id = " + taskDto.getBoardId() +
                    "  не принадлежит данному пользователю. Доступ к задачам других пользователей ограничен");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskDto))
                            .with(signInWithUser("user2", "pass2")))
                    .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createTask when title is null then return bad request status")
        public void testCreateTask_whenTitleIsNull_thenReturnBadRequest() throws Exception {
            TaskDto taskDto = TaskDto.builder()
                    .body("Add tests for TaskController class")
                    .boardId(1L)
                    .status(Status.IN_WAITING)
                    .priority(Priority.MEDIUM)
                    .completeBefore(LocalDateTime.now().plusDays(2))
                    .build();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Не указан заголовок задачи");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskDto))
                            .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createTask when title is empty then return bad request status")
        public void testCreateTask_whenTitleIsEmpty_thenReturnBadRequest() throws Exception {
            TaskDto taskDto = TaskDto.builder()
                    .title("")
                    .body("Add tests for TaskController class")
                    .boardId(1L)
                    .status(Status.IN_WAITING)
                    .priority(Priority.MEDIUM)
                    .completeBefore(LocalDateTime.now().plusDays(2))
                    .build();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Не указан заголовок задачи");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskDto))
                            .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createTask when boardId is null then return bad request status")
        public void testCreateTask_whenBoardIdIsNull_thenReturnBadRequest() throws Exception {
            TaskDto taskDto = TaskDto.builder()
                    .title("Add tests")
                    .body("Add tests for TaskController class")
                    .status(Status.IN_WAITING)
                    .priority(Priority.MEDIUM)
                    .completeBefore(LocalDateTime.now().plusDays(2))
                    .build();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Не указан идентификатор доски к которой привязана данная задача");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskDto))
                            .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test createTask when boardId specified incorrectly then return bad request status")
        public void testCreateTask_whenBoardIdIsNotCorrect_thenReturnBadRequest() throws Exception {
            TaskDto taskDto = TaskDto.builder()
                    .title("Add tests")
                    .body("Add tests for TaskController class")
                    .boardId(0L)
                    .status(Status.IN_WAITING)
                    .priority(Priority.MEDIUM)
                    .completeBefore(LocalDateTime.now().plusDays(2))
                    .build();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Не корректное значение идентификатор доски");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskDto))
                            .with(signInWithUser("testuser", "testpassword")))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

    }

    @Nested
    class GetAllTasksByBoard {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllTasksByBoard when send valid request then return Page Of TaskDto")
        public void testGetAllTasksByBoard_whenSendValidRequest_thenReturnPageOfTaskDto() throws Exception {
            final String boardId = "1";
            final String sortProperty = "title";
            final JSONObject page = new JSONObject("{\"number\":0,\"size\":10,\"totalPages\":1,\"totalElements\":2}");
            final JSONObject expected = new JSONObject();
            expected.put("page", page);

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks")
                            .param("board", boardId)
                            .param("size", "10")
                            .param("page", "0")
                            .param("dir", "DESC")
                            .param("sortBy", sortProperty)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            Assertions.assertEquals(HttpStatus.OK.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllTasksByBoard when send request without specified pageable parameters " +
                "then return page of taskDto with defaults parameters")
        public void testGetAllTasksByBoard_whenSendRequestWithoutPageableParams_thenReturnPageOfTaskDto() throws Exception {
            final String boardId = "1";
            final JSONObject page = new JSONObject("{\"number\":0,\"size\":10,\"totalPages\":1,\"totalElements\":2}");
            final JSONObject expected = new JSONObject();
            expected.put("page", page);

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks")
                            .param("board", boardId)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            Assertions.assertEquals(HttpStatus.OK.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllTasksByBoard when user is not the owner of the board whit tasks then return forbidden status")
        public void testGetAllTasksByBoard_whenUserIsNotOwnerOfBoard_thenReturnForbidden() throws Exception {
            final String boardId = "1";
            final String sortProperty = "title";
            final JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.FORBIDDEN.value());

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks")
                            .param("board", boardId)
                            .param("size", "10")
                            .param("page", "0")
                            .param("dir", "DESC")
                            .param("sortBy", sortProperty)
                            .with(signInWithUser("user2", "pass2")))
                    .andReturn();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllTasksByBoard when boardId is not correct then return bad request status")
        public void testGetAllTasksByBoard_whenBoardIdIsNotCorrect_thenReturnBadRequest() throws Exception {
            final String boardId = "0";
            final String sortProperty = "title";
            final JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Некорректный идентификатор доски");

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks")
                            .param("board", boardId)
                            .param("size", "10")
                            .param("page", "0")
                            .param("dir", "DESC")
                            .param("sortBy", sortProperty)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }


        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllTasksByBoard when page size is less than 1 then return bad request status")
        public void testGetAllTasksByBoard_whenSizeIsLessThan1_thenReturnBadRequest() throws Exception {
            final String boardId = "1";
            final String sortProperty = "title";
            final JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Размер станицы не может быть меньше единицы");

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks")
                            .param("board", boardId)
                            .param("size", "0")
                            .param("page", "0")
                            .param("dir", "DESC")
                            .param("sortBy", sortProperty)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }


        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllTasksByBoard when page size is more than 100 then return bad request status")
        public void testGetAllTasksByBoard_whenSizeIsMoreThan100_thenReturnBadRequest() throws Exception {
            final String boardId = "1";
            final String sortProperty = "title";
            final JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Размер страницы не должен превышать 100 записей");

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks")
                            .param("board", boardId)
                            .param("size", "101")
                            .param("page", "0")
                            .param("dir", "DESC")
                            .param("sortBy", sortProperty)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllTasksByBoard when page number is less than 0 then return bad request status")
        public void testGetAllTasksByBoard_whenPageNumIsLessThan0_thenReturnBadRequest() throws Exception {
            final String boardId = "1";
            final String sortProperty = "title";
            final JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Некорректное значение номера страницы");

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks")
                            .param("board", boardId)
                            .param("size", "100")
                            .param("page", "-1")
                            .param("dir", "DESC")
                            .param("sortBy", sortProperty)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllTasksByBoard when sort direction is not valid then return bad request status")
        public void testGetAllTasksByBoard_whenSortDirectionIsNotValid_thenReturnBadRequest() throws Exception {
            final String boardId = "1";
            final String sortProperty = "title";
            final JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Неверно указано направление сортировки. Допустимые значения ASC или DESC");

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks")
                            .param("board", boardId)
                            .param("size", "100")
                            .param("page", "0")
                            .param("dir", "NOT_VALID_DIRECTION")
                            .param("sortBy", sortProperty)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getAllTasksByBoard when sort property is not valid then return bad request status")
        public void testGetAllTasksByBoard_whenSortPropertyIsNotValid_thenReturnBadRequest() throws Exception {
            final String boardId = "1";
            final String sortProperty = "NOT_VALID_SORT_PROPERTY";
            final JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Сортировка по указанному полю не поддерживается");

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks")
                            .param("board", boardId)
                            .param("size", "100")
                            .param("page", "0")
                            .param("dir", "ASC")
                            .param("sortBy", sortProperty)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }
    }

    @Nested
    class GetTaskByIdTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getTaskById when send valid request then return taskDto")
        public void testGetTaskById_whenSendValidRequest_thenReturnTaskDto() throws Exception {
            final long taskId = 1L;
            String expected = "{\"id\":1,\"title\":\"Мероприятие\",\"body\":\"Посетить мероприятие\"," +
                    "\"priority\":\"MEDIUM\",\"status\":\"IN_PROGRESS\",\"completeBefore\":\"2025-07-23T12:22:00\"," +
                    "\"completeDate\":null,\"boardId\":1,\"createDate\":null,\"updateDate\":\"2025-07-22T00:00:00\"}";

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/tasks/" + taskId)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            int actualStatus = result.getResponse().getStatus();
            String actual = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
            System.out.println(actual);

            Assertions.assertEquals(HttpStatus.OK.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getTaskById when user is not owner for task then return forbidden status")
        public void testGetTaskById_whenUserIsNotOwnerForTask_thenReturnForbidden() throws Exception {
            final long taskId = 1L;
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.FORBIDDEN.value());

            final MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/tasks/" + taskId)
                            .with(signInWithUser("user2", "pass2")))
                    .andReturn();
            int actualStatus = result.getResponse().getStatus();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }


        @Test
        @Sql("test_data.sql")
        @DisplayName("Test getTaskById when taskId less than 1 then return bad request status")
        public void testGetTaskById_whenTaskIdLessThan1_thenReturnBadRequest() throws Exception {
            final long taskId = 0L;
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());

            final MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/tasks/" + taskId)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            int actualStatus = result.getResponse().getStatus();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

    }

    @Nested
    class UpdateTaskTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test updateTask when send valid request then task success updated")
        public void testUpdateTask_whenSendValidRequest_thenTaskSuccessUpdated() throws Exception {
            final long taskId = 1L;
            Task task = taskRepository.findById(taskId).orElseThrow();
            task.setTitle("Meropriyatie");
            task.setBody("Posetit meropriyatie");
            task.setCompleteDate(LocalDateTime.now());
            task.setStatus(Status.COMPLETED);
            task.setPriority(Priority.HIGH);
            TaskDto taskDto = taskMapper.map(task);
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.OK.value());
            expected.put("message", "Задача " + taskDto.getTitle() + " успешно обновлена");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskDto))
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            Task actualTask = taskRepository.findById(taskId).orElseThrow();

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            Assertions.assertEquals(task.getTitle(), actualTask.getTitle());
            Assertions.assertEquals(task.getBody(), actualTask.getBody());
            Assertions.assertNotNull(actualTask.getCompleteDate());
            Assertions.assertEquals(task.getStatus(), actualTask.getStatus());
            Assertions.assertEquals(task.getPriority(), actualTask.getPriority());
            Assertions.assertTrue(actualTask.getUpdateDate().isAfter(actualTask.getCreateDate()));

        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test updateTask when user is not task owner then return forbidden status")
        public void testUpdateTask_whenUserIsNotTaskOwner_thenReturnForbidden() throws Exception {
            final long taskId = 1L;
            Task task = taskRepository.findById(taskId).orElseThrow();
            task.setTitle("newTitle");
            final TaskDto taskDto = taskMapper.map(task);
            final JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.FORBIDDEN.value());

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskDto))
                            .with(signInWithUser("user2", "pass2")))
                    .andReturn();

            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test updateTask when taskId is null then return bad request status")
        public void testUpdateTask_whenTaskIdIsNull_thenReturnBadRequest() throws Exception {
            final long taskId = 1L;
            Task task = taskRepository.findById(taskId).orElseThrow();
            TaskDto taskDto = taskMapper.map(task);
            taskDto.setId(null);
            final JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.NOT_FOUND.value());

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskDto))
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();

            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final int actualStatus = result.getResponse().getStatus();

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), actualStatus);
        }

    }

    @Nested
    class DeleteTaskByIdTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test deleteTaskById when send valid request then task success deleted")
        public void testDeleteTaskById_whenSendValidRequest_thenTaskSuccessDeleted() throws Exception {
            final long taskId = 1L;
            final long beforeCount = taskRepository.count();
            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/tasks/" + taskId)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final int actualStatus = result.getResponse().getStatus();
            final long afterCount = taskRepository.count();

            Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), actualStatus);
            Assertions.assertEquals(1, beforeCount - afterCount);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test deleteTaskById when user is not task owner then return forbidden status")
        public void testDeleteTaskById_whenUserIsNotTaskOwner_thenReturnForbidden() throws Exception {
            final long taskId = 1L;
            final long beforeCount = taskRepository.count();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.FORBIDDEN.value());

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/tasks/" + taskId)
                            .with(signInWithUser("user2", "pass2")))
                    .andReturn();
            final int actualStatus = result.getResponse().getStatus();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final long afterCount = taskRepository.count();

            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            Assertions.assertEquals(beforeCount, afterCount);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test deleteTaskById when taskId is not correct then return bad request status")
        public void testDeleteTaskById_whenTaskIdIsNotCorrect_thenReturnBadRequest() throws Exception {
            final long taskId = 0L;
            final long beforeCount = taskRepository.count();
            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());

            final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/tasks/" + taskId)
                            .with(signInWithUser("testuser", "testpassword")))
                    .andReturn();
            final int actualStatus = result.getResponse().getStatus();
            final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            final long afterCount = taskRepository.count();

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            Assertions.assertEquals(beforeCount, afterCount);
        }


    }

}
