package com.emobile.springtodo.service.scheduler;


import com.emobile.springtodo.entity.RefreshToken;
import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.repository.RefreshTokenRepository;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.security.SecurityRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;


import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;

@SpringBootTest
@ActiveProfiles("test")
public class SchedulerTaskServiceTest {

    @SpyBean
    private ScheduledTaskService scheduledTaskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Container
    public static final PostgreSQLContainer<?> POSTGRES
            = new PostgreSQLContainer<>("postgres:17.5");

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.scheduler.expirationTokenDeleteInterval", () -> "PT7S");
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
    public void fillDatabase() {
        ServiceUser user = ServiceUser.builder()
                .username("User1")
                .email("user1@email.com")
                .password("UserPassword")
                .roles(Set.of(SecurityRole.ROLE_USER))
                .build();
        userRepository.save(user);
    }

    @AfterEach
    public void clearDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "refresh_tokens", "service_users");
    }

    @Test
    @DisplayName("Test deleteExpiredRefreshTokens")
    public void testDeleteExpiredRefreshTokens() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("this-test-refresh-token")
                .userId(userRepository.findByUsernameOrEmail("User1", "user1@email.com").orElseThrow().getId())
                .expireDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);
        long afterAddTokenCount = refreshTokenRepository.count();
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    verify(scheduledTaskService, atLeast(2)).deleteExpiredRefreshTokens();
                    Assertions.assertEquals(afterAddTokenCount-1, refreshTokenRepository.count());
                });
    }

}
