package com.emobile.springtodo.service.scheduler;


import com.emobile.springtodo.AbstractIntegrationTest;
import com.emobile.springtodo.entity.RefreshToken;
import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.repository.RefreshTokenRepository;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.security.SecurityRole;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

class SchedulerTaskServiceTest extends AbstractIntegrationTest {

    @SpyBean
    private ScheduledTaskService scheduledTaskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void fillDatabase() {
        ServiceUser user = ServiceUser.builder()
                .username("User1")
                .email("user1@email.com")
                .password("UserPassword")
                .roles(Set.of(SecurityRole.ROLE_USER))
                .build();
        userRepository.save(user);
    }

    @AfterEach
    void clearDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "refresh_tokens", "service_users");
    }

    @Test
    @DisplayName("Test deleteExpiredRefreshTokens")
    void testDeleteExpiredRefreshTokens() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("this-test-refresh-token")
                .userId(userRepository.findByUsernameOrEmail("User1", "user1@email.com").orElseThrow().getId())
                .expireDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);
        long beforeTokenCount = refreshTokenRepository.count();
        await().atMost(Duration.ofSeconds(11))
                .untilAsserted(() -> {
                    verify(scheduledTaskService, atLeast(2)).deleteExpiredRefreshTokens();
                    Assertions.assertTrue(beforeTokenCount > refreshTokenRepository.count());
                });
    }

}
