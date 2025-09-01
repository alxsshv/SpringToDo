package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.request.LoginRequest;
import com.emobile.springtodo.dto.request.RefreshTokenRequest;
import com.emobile.springtodo.dto.request.RegisterUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.nio.charset.StandardCharsets;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Container
    public static final PostgreSQLContainer<?> POSTGRES
            = new PostgreSQLContainer<>("postgres:17.5");

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeAll
    public static void startDatabase() {
        POSTGRES.start();
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
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "refresh_tokens", "service_users");
    }

    @AfterAll
    public static void stopDatabase() {
        POSTGRES.stop();
    }


    @Nested
    class LoginMethodTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test login method when send valid request then user success authorized")
        public void testLogin_whenSendValidRequest_thenUserSuccessAuthorized() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .usernameOrEmail("user2")
                    .password("pass2")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("id", 2);
            expectedResponse.put("username", "user2");
            expectedResponse.put("email", "user2@email.com");

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
            int actualStatus = mvcResult.getResponse().getStatus();
            JSONObject actualResponse = new JSONObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.OK.value(), actualStatus);
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
        }



        @Test
        @Sql("test_data.sql")
        @DisplayName("Test login method when username not found then return unauthorized")
        public void testLogin_whenUsernameNotFound_thenReturnUnauthorized() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .usernameOrEmail("user999")
                    .password("password")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.UNAUTHORIZED.value());

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
            int actualStatus = mvcResult.getResponse().getStatus();
            JSONObject actualResponse = new JSONObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), actualStatus);
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test login method when send invalid password then return unauthorized")
        public void testLogin_whenSendInvalidPassword_thenReturnUnauthorized() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .usernameOrEmail("user2")
                    .password("InvalidPassword")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            expectedResponse.put("message", "Bad credentials");
            expectedResponse.put("error", "Unauthorized");

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
            int actualStatus = mvcResult.getResponse().getStatus();
            JSONObject actualResponse = new JSONObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), actualStatus);
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test login method when username is empty then return bad request status")
        public void testLogin_whenUsernameIsEmpty_thenReturnBadRequest() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .usernameOrEmail("")
                    .password("pass2")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Имя пользователя не может быть пустым");

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
            int actualStatus = mvcResult.getResponse().getStatus();
            JSONObject actualResponse = new JSONObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test login method when username is null then return bad request status")
        public void testLogin_whenUsernameIsNull_thenReturnBadRequest() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .password("pass2")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Имя пользователя не может быть пустым");

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
            int actualStatus = mvcResult.getResponse().getStatus();
            JSONObject actualResponse = new JSONObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test login method when password is empty then return bad request status")
        public void testLogin_whenPasswordIsEmpty_thenReturnBadRequest() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .usernameOrEmail("user2")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Пароль не может быть пустым");

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
            int actualStatus = mvcResult.getResponse().getStatus();
            JSONObject actualResponse = new JSONObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test login method when password is less than  then return bad request status")
        public void testLogin_whenPasswordIsLessThan4_thenReturnBadRequest() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .usernameOrEmail("user2")
                    .password("pas")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Пароль должен содержать не менее 4 и не более 50 символов");

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
            int actualStatus = mvcResult.getResponse().getStatus();
            JSONObject actualResponse = new JSONObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test login method when password is more than 50 then return bad request status")
        public void testLogin_whenPasswordIsMoreThan50_thenReturnBadRequest() throws Exception {
            LoginRequest loginRequest = LoginRequest.builder()
                    .usernameOrEmail("user2")
                    .password("verylongpasswordForUser_veryverylongpasswordforuser")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Пароль должен содержать не менее 4 и не более 50 символов");

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
            int actualStatus = mvcResult.getResponse().getStatus();
            System.out.println(actualStatus);
            System.out.println(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            JSONObject actualResponse = new JSONObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
        }
    }

    @Nested
    class RegisterMethodTest {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when send valid request then user success register")
        public void testRegister_whenSendValidRequest_thenUserSuccessRegister() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("newuser")
                    .email("newuser@email.com")
                    .password("newuserpass")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.OK.value());
            expectedResponse.put("message", "Пользователь "+ request.getUsername() +" успешно зарегистрирован");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.OK.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when username is null then return bad request")
        public void testRegister_whenUsernameIsNull_thenReturnBadRequest() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .email("newuser@email.com")
                    .password("newuserpass")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Имя пользователя не может быть пустым");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();
            System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }


        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when username is less than 3 then return bad request")
        public void testRegister_whenUsernameIsLessThan3_thenReturnBadRequest() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("us")
                    .email("newuser@email.com")
                    .password("newuserpass")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Имя пользователя должно содержать не менее 3 и не более 50 символов");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when username is more than 50 then return bad request")
        public void testRegister_whenUsernameIsMoreThan50_thenReturnBadRequest() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("veryverylongusername_veryverylongusername_verylongu")
                    .email("newuser@email.com")
                    .password("newuserpass")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Имя пользователя должно содержать не менее 3 и не более 50 символов");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when email is null then return bad request")
        public void testRegister_whenEmailIsNull_thenReturnBadRequest() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("newUsername")
                    .password("newuserpass")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Пожалуйста укажите адрес электронной почты");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();
            System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when email is not correct then return bad request")
        public void testRegister_whenEmailIsNotCorrect_thenReturnBadRequest() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("newUsername")
                    .email("newuseremail.com")
                    .password("newuserpass")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Неверный формат адреса электронной почты. " +
                    "Пожалуйста проверьте правильность указанного email");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when email already exists then return bad request")
        public void testRegister_whenEmailIsAlreadyExists_thenReturnBadRequest() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("newUsername")
                    .email("user2@email.com")
                    .password("newuserpass")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Email уже используется");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();
            System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }


        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when password is null then return bad request")
        public void testRegister_whenPasswordIsNull_thenReturnBadRequest() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("newUsername")
                    .email("newuser@email.com")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Пароль не может быть пустым");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when password length is less than 4 then return bad request")
        public void testRegister_whenPasswordLengthIsLessThan4_thenReturnBadRequest() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("newUsername")
                    .email("newuser@email.com")
                    .password("pas")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Пароль должен содержать не менее 4 и не более 50 символов");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test register method when password length is more than 50 then return bad request")
        public void testRegister_whenPasswordLengthIsMoreThan50_thenReturnBadRequest() throws Exception {
            final RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("newUsername")
                    .email("newUser@email.com")
                    .password("VeryVeryLongPasswordForUser_VeryVeryLongUserPassword")
                    .build();
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "Пароль должен содержать не менее 4 и не более 50 символов");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = result.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }
    }

    @Nested
    class RefreshTokenMethodTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test refreshTokenMethod when send valid request then return new refresh-token")
        public void testRefreshTokenMethod_whenSendValidRequest_thenReturnNewRefreshToken() throws Exception {
            MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("user2", "pass2"))))
                    .andReturn();
            JSONObject loginResponse = new JSONObject(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            String refreshToken = loginResponse.getString("refreshToken");

            MvcResult refreshTokenRequestResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                    .andReturn();
            int actualStatus = refreshTokenRequestResult.getResponse().getStatus();
            JSONObject refreshTokenResponse = new JSONObject(refreshTokenRequestResult.getResponse()
                    .getContentAsString(StandardCharsets.UTF_8));
            String actualRefreshToken = refreshTokenResponse.getString("refreshToken");
            String actualAccessToken = refreshTokenResponse.getString("accessToken");

            Assertions.assertNotNull(actualRefreshToken);
            Assertions.assertNotNull(actualAccessToken);
            Assertions.assertEquals(HttpStatus.OK.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test refreshTokenMethod when send expired refresh-token then return unauthorized status")
        public void testRefreshTokenMethod_whenSendExpiredRefreshToken_thenReturnUnauthorized() throws Exception {
            String refreshToken = "USER2_TOKEN1";
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.UNAUTHORIZED.value());
            expectedResponse.put("message", "Срок действия токена USER2_TOKEN1 истек." +
                    " Пожалуйста авторизуйтесь при помощи логина и пароля");

            MvcResult refreshTokenRequestResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(refreshTokenRequestResult.getResponse()
                    .getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = refreshTokenRequestResult.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), actualStatus);
        }

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test refreshTokenMethod when refresh-token is empty then return bad request status")
        public void testRefreshTokenMethod_whenSendExpiredRefreshToken_thenReturnBadRequestStatus() throws Exception {
            String refreshToken = "";
            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.BAD_REQUEST.value());
            expectedResponse.put("message", "refresh-token не может быть пустым");

            MvcResult refreshTokenRequestResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(refreshTokenRequestResult.getResponse()
                    .getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = refreshTokenRequestResult.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), actualStatus);
        }

    }

    @Nested
    class LogoutMethodTests {

        @Test
        @Sql("test_data.sql")
        @DisplayName("Test logout when send valid request then user success logout")
        public void testLogout_whenSendValidRequest_thenUserSuccessLogout() throws Exception {
            String username = "user2";
            String password = "pass2";
            MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                    .andReturn();
            JSONObject loginResponse = new JSONObject(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            String accessToken = loginResponse.getString("accessToken");


            JSONObject expectedResponse = new JSONObject();
            expectedResponse.put("statusCode", HttpStatus.OK.value());
            expectedResponse.put("message", "Пользовать " + username + " завершил работу с сервисом");

            MvcResult logoutResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/logout")
                            .header("Authorization", "Bearer "+ accessToken))
                    .andReturn();
            JSONObject actualResponse = new JSONObject(logoutResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
            int actualStatus = logoutResult.getResponse().getStatus();

            JSONAssert.assertEquals(expectedResponse, actualResponse, JSONCompareMode.LENIENT);
            Assertions.assertEquals(HttpStatus.OK.value(), actualStatus);
        }

    }


}
