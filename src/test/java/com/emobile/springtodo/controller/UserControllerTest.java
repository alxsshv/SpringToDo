package com.emobile.springtodo.controller;

import com.emobile.springtodo.configuration.AppDefaults;
import com.emobile.springtodo.dto.request.CreateUserRequest;
import com.emobile.springtodo.dto.request.UpdateUserRequest;
import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.security.SecurityRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Aleksey Shvariov
 */

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private static final PostgreSQLContainer<?> POSTGRES
            = new PostgreSQLContainer<>("postgres:17.5");

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    public static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeAll
    public static void startDatabase() {
        POSTGRES.start();
    }



    @AfterAll
    public static void stopDatabase() {
        POSTGRES.stop();
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
        userRepository.deleteAll();
    }

    @Nested
    class CreateUserMethodTests {

        @Test
        @Sql({"test_data.sql"})
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when send valid createUserRequest then user created")
        public void testCreateUser_whenSendValidRequest_thenUserCreated() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("username33")
                    .email("user33@email.com")
                    .password("password33")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.CREATED.value());
            expected.put("message", "Создан новый пользователь " + request.getUsername());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(HttpStatus.CREATED.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when username already exists then return bad request status")
        public void testCreateUser_whenUsernameAlreadyExist_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("user2")
                    .email("user33@email.com")
                    .password("password33")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Пользователь с таким именем пользователя уже зарегистрирован в сервисе");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when username is null then return bad request status")
        public void testCreateUser_whenUsernameIsNull_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .email("user33@email.com")
                    .password("password33")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Имя пользователя не может быть пустым");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when username is empty then return bad request status")
        public void testCreateUser_whenUsernameIsEmpty_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("")
                    .email("user33@email.com")
                    .password("password33")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Имя пользователя не может быть пустым");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when email already exists then return bad request status")
        public void testCreateUser_whenEmailAlreadyExist_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("user44")
                    .email("user2@email.com")
                    .password("password33")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Email уже используется");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when email is null then return bad request status")
        public void testCreateUser_whenEmailIsNullWhenReturnBadRequestStatus() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("user33")
                    .password("password33")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Адрес электронной почты не может быть пустым");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

        }


        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when email is empty then return bad request status")
        public void testCreateUser_whenEmailIsEmpty_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("user33")
                    .email("")
                    .password("password33")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Адрес электронной почты не может быть пустым");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when email has not correct format email then return bad request status")
        public void testCreateUser_whenEmailIsNotCorrect_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("user33")
                    .email("user33email.com")
                    .password("password33")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Неверный формат адреса электронной почты");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);


        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when password length is less than 4 then return bad request status")
        public void testCreateUser_whenPasswordLengthIsLessThan4_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("user33")
                    .email("user33@email.com")
                    .password("pas")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Пароль должен содержать не менее 4 и не более 50 символов");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when password length is more than 50 then return bad request status")
        public void testCreateUser_whenPasswordLengthIsMoreThan50_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("user33")
                    .email("user33@email.com")
                    .password("verylongpasswordbyuser_veryverylongpasswordbyuser33")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Пароль должен содержать не менее 4 и не более 50 символов");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(actual);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when password is null then return bad request status")
        public void testCreateUser_whenPasswordIsNull_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("user33")
                    .email("user33@email.com")
                    .roleNames(Set.of(SecurityRole.ROLE_USER.name(), SecurityRole.ROLE_ADMIN.name()))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", 400);
            expected.put("message", "Пароль не может быть пустым");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(actual);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test createUser when roles is empty then return bad request status")
        public void testCreateUser_whenRolesIsEmpty_thenReturnBadRequest() throws Exception {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("user33")
                    .email("user33@email.com")
                    .password("password33")
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Необходимо указать одну или несколько ролей пользователя. Доступные роли ROLE_ADMIN, ROLE_USER");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(actual);
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }
    }

    @Nested
    class GetAllUsersTests {

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test getAllUsers when send request with valid parameters then return page of users")
        public void testGetAllUsers_whenSendRequestWithValidParameters_thenReturnPageOfUsers() throws Exception {
            String size = "2";
            String page = "0";
            String dir = "ASC";
            String sortProperty = "username";

            JSONObject pageInfo = new JSONObject();
            pageInfo.put("size", 2);
            pageInfo.put("number", 0);
            pageInfo.put("totalElements", 3);
            pageInfo.put("totalPages", 2);
            JSONObject expected = new JSONObject();
            expected.put("page", pageInfo);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/users")
                            .param("size", size)
                            .param("page", page)
                            .param("dir", dir)
                            .param("sortBy", sortProperty))
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andReturn();

            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test getAllUser when not specified pageable parameters then return page of users with default parameters")
        public void testGetAllUsers_whenNotSpecifiedPageableParameters_thenReturnPageOfUsers() throws Exception {
            JSONObject pageInfo = new JSONObject();
            pageInfo.put("size", Integer.parseInt(AppDefaults.PAGE_SIZE));
            pageInfo.put("number", 0);
            pageInfo.put("totalElements", 3);
            pageInfo.put("totalPages", 1);
            JSONObject expected = new JSONObject();
            expected.put("page", pageInfo);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/users"))
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andReturn();

            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test getAllUsers when size is less than 1 then return bad request status")
        public void testGetAllUsers_whenSizeLessThan1_thenReturnBadRequest() throws Exception {
            String size = "0";
            String page = "0";
            String dir = "ASC";
            String sortProperty = "username";

            JSONObject expected = new JSONObject();
            expected.put("statusCode", 400);
            expected.put("message", "Размер страницы не может быть меньше нуля");
            System.out.println(expected);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/users")
                            .param("size", size)
                            .param("page", page)
                            .param("dir", dir)
                            .param("sortBy", sortProperty))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
           JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

           JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test getAllUsers when size is more than 100 then return bad request status")
        public void testGetAllUsers_whenSizeMoreThan100_thenReturnBadRequest() throws Exception {
            String size = "101";
            String page = "0";
            String dir = "ASC";
            String sortProperty = "username";

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Размер страницы не должен превышать 100 записей");

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/users")
                            .param("size", size)
                            .param("page", page)
                            .param("dir", dir)
                            .param("sortBy", sortProperty))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();

            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test getAllUsers when page number is less than 0 then return bad request status")
        public void testGetAllUsers_whenPageNumIsLess0_thenReturnBadRequest() throws Exception {
            String size = "2";
            String page = "-1";
            String dir = "ASC";
            String sortProperty = "username";

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Некорректное значение номера страницы");

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/users")
                            .param("size", size)
                            .param("page", page)
                            .param("dir", dir)
                            .param("sortBy", sortProperty))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();

            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test getAllUsers when direction has not correct value then return bad request status")
        public void testGetAllUsers_whenDirectionNotCorrect_thenReturnBadRequest() throws Exception {
            String size = "2";
            String page = "0";
            String dir = "NOT_CORRECT_DIRECTION";
            String sortProperty = "username";

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Неверно указано направление сортировки. Допустимые значения ASC или DESC");

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/users")
                            .param("size", size)
                            .param("page", page)
                            .param("dir", dir)
                            .param("sortBy", sortProperty))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();

            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Test getAllUsers when sort user properties is not valid then return bad request status")
        public void testGetAllUsers_whenSortByIsNotValid_ThenReturnBadRequestStatus() throws Exception {
            String size = "2";
            String page = "0";
            String dir = "DESC";
            String sortProperty = "NOT_CORRECT_SERVICE_USER_FIELD";

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expected.put("message", "Сортировка по указанному полю не поддерживается");

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/v1/users")
                            .param("size", size)
                            .param("page", page)
                            .param("dir", dir)
                            .param("sortBy", sortProperty))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();

            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }
    }

        @Nested
        class GetUserByIdTests {

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test getUserById when send valid request then return service user")
            public void testGetUserById_whenSendValidRequest_thenReturnServiceUser() throws Exception {
                long userId = 1L;
                JSONObject expected = new JSONObject();
                expected.put("id", userId);

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/" + userId))
                        .andExpect(status().is(HttpStatus.OK.value()))
                        .andReturn();

                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                System.out.println(actual);

                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test getUserById when send not correct id then return bad request status")
            public void testGetAllUsersById_whenSendNotCorrectId_thenReturnBadRequest() throws Exception {
                long userId = -1L;
                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
                expected.put("message", "ID пользователя должен быть больше нуля");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/" + userId))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();

                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                System.out.println(actual);

                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

        }

        @Nested
        class UpdateUserTests {

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when send valid request then user success updated")
            public void testUpdateUser_whenSendValidRequest_thenUserSuccessUpdated() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("user2")
                        .email("newUser2@email.com")
                        .password("newpass2")
                        .roleNames(Set.of("ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.OK.value());
                expected.put("message","Сведения о пользователе user2 успешно обновлены");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(HttpStatus.OK.value()))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

                Optional<ServiceUser> userOpt = userRepository.findById(userId);
                ServiceUser userFromDb = userOpt.orElseThrow();

                Assertions.assertEquals(request.getEmail(), userFromDb.getEmail());
                Assertions.assertTrue(passwordEncoder.matches(request.getPassword(), userFromDb.getPassword()));
                Set<String> userFromDbRolesNames = userFromDb.getRoles().stream().map(SecurityRole::name).collect(Collectors.toSet());
                Assertions.assertEquals(request.getRoleNames(), userFromDbRolesNames);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when username is null then return bad request status")
            public void testUpdateUser_whenUsernameIsNull_thenReturnBadRequest() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .email("newUser2@email.com")
                        .password("newpass2")
                        .roleNames(Set.of("ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
                expected.put("message","Имя пользователя не может быть пустым");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when username is empty then return bad request status")
            public void testUpdateUser_whenUsernameIsEmpty_thenReturnBadRequest() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("")
                        .email("newUser2@email.com")
                        .password("newpass2")
                        .roleNames(Set.of("ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
                expected.put("message","Имя пользователя не может быть пустым");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when the user name does not match the specified id then return bad request status")
            public void testUpdateUser_whenUsernameNotMatchId_thenReturnBadRequest() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("testuser")
                        .email("newUser2@email.com")
                        .password("newpass2")
                        .roleNames(Set.of("ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when email already exists then return bad request status")
            public void testUpdateUser_whenEmailAlreadyExists_thenReturnBadRequest() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("user2")
                        .email("testuser@email.com")
                        .password("newpass2")
                        .roleNames(Set.of("ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when email is null then return bad request status")
            public void testUpdateUser_whenEmailIsNull_thenReturnBadRequest() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("user2")
                        .password("newpass2")
                        .roleNames(Set.of("ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
                expected.put("message", "Адрес электронной почты не может быть пустым");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when email has not correct format then return bad request status")
            public void testUpdateUser_whenEmailHasNotCorrectFormat_thenReturnBadRequest() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("user2")
                        .email("user2email.com")
                        .password("newpass2")
                        .roleNames(Set.of("ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
                expected.put("message", "Неверный формат адреса электронной почты");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when password length is less than 4 then return bad request status")
            public void testUpdateUser_whenPasswordLengthLess4_thenReturnBadRequest() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("user2")
                        .email("newuser2@email.com")
                        .password("pas")
                        .roleNames(Set.of("ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
                expected.put("message", "Пароль должен содержать не менее 4 и не более 50 символов");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when password length is more than 50 then return bad request status")
            public void testUpdateUser_thenPasswordLengthMoreThan50_thenReturnBadRequest() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("user2")
                        .email("newuser2@email.com")
                        .password("verylongpasswordforuser_veryverylongpasswordforuser")
                        .roleNames(Set.of("ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
                expected.put("message", "Пароль должен содержать не менее 4 и не более 50 символов");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

            @Test
            @Sql("test_data.sql")
            @WithMockUser(roles = {"ADMIN"})
            @DisplayName("Test updateUser when roles is not specified then return bad request")
            public void testUpdateUser_whenRolesIsNotSpecified_thenReturnBadRequest() throws Exception {
                final long userId = 2L;
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("user2")
                        .email("newuser2@email.com")
                        .password("newpassword2")
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.BAD_REQUEST.value());
                expected.put("message", "Необходимо указать одну или несколько ролей пользователя." +
                        " Доступные роли ROLE_ADMIN, ROLE_USER");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/" + userId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();
                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

        }

        @Nested
        class UpdateAccountTest {

            @Test
            @Sql("test_data.sql")
            @WithMockUser(username = "user3")
            @DisplayName("Test updateAccount when has been sent request to update someone else's account then return forbidden status")
            public void testUpdateAccount_whenHasBeenSentRequestToUpdateSomeoneElseAccount_thenReturnForbidden() throws Exception {
                UpdateUserRequest request = UpdateUserRequest.builder()
                        .username("user2")
                        .email("newuser2@email.com")
                        .password("newpassword2")
                        .roleNames(Set.of("ROLE_USER", "ROLE_ADMIN"))
                        .build();

                JSONObject expected = new JSONObject();
                expected.put("statusCode", HttpStatus.FORBIDDEN.value());
                expected.put("message", "Неверно указано имя пользователя." +
                        " Имя пользователя не доступно для изменения. Пользователь может изменять данные только своего аккаунта.");

                MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/my")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                        .andReturn();

                JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
            }

        @Test
        @Sql("test_data.sql")
        @WithMockUser(username = "user2")
        @DisplayName("Test updateAccount when send valid request then user success update")
        public void testUpdateAccount_whenSendValidRequest_thenUserSuccessUpdate() throws Exception {
            final long userId = 2L;
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .username("user2")
                    .email("newuser2@email.com")
                    .password("newpassword2")
                    .roleNames(Set.of("ROLE_USER", "ROLE_ADMIN"))
                    .build();

            JSONObject expected = new JSONObject();
            expected.put("statusCode", HttpStatus.OK.value());
            expected.put("message", "Сведения о пользователе user2 успешно обновлены");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/my")
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andReturn();

            JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

            Optional<ServiceUser> userOpt = userRepository.findById(userId);
            ServiceUser userFromDb = userOpt.orElseThrow();
            Set<String> userFromDbRolesNames = userFromDb.getRoles().stream().map(SecurityRole::name).collect(Collectors.toSet());

            Assertions.assertEquals(request.getEmail(), userFromDb.getEmail());
            Assertions.assertTrue(passwordEncoder.matches(request.getPassword(), userFromDb.getPassword()));
            Assertions.assertNotEquals(request.getRoleNames(), userFromDbRolesNames);
        }

            @Nested
            class DeleteUserTests {

                @Test
                @Sql("test_data.sql")
                @WithMockUser(roles = {"ADMIN"})
                @DisplayName("Test deleteUser when send valid request then user success deleted")
                public void testDeleteUser_whenSendValidRequest_thenUserSuccessDeleted() throws Exception {
                    final long userId = 1L;
                    final long beforeUsersCount = userRepository.count();

                    final JSONObject expected = new JSONObject();
                    expected.put("statusCode", HttpStatus.NO_CONTENT.value());
                    expected.put("message", "Сведения о пользователе c id = " + userId + " успешно удалены");

                    final MvcResult result = mockMvc
                            .perform(MockMvcRequestBuilders.delete("/api/v1/users/" + userId))
                            .andExpect(status().is(HttpStatus.NO_CONTENT.value()))
                            .andReturn();

                    final long afterUsersCount = userRepository.count();

                    final JSONObject actual = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

                    JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
                    Assertions.assertEquals(beforeUsersCount, afterUsersCount + 1);
                }
            }

        }

}
