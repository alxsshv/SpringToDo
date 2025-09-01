package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.RefreshToken;
import com.emobile.springtodo.repository.impl.RefreshTokenRepositoryImpl;
import com.emobile.springtodo.repository.impl.mapper.RefreshTokenRowMapper;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * @author Aleksey Shvariov
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RefreshTokenRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final PostgreSQLContainer<?> POSTGRES
            = new PostgreSQLContainer<>("postgres");

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
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

    @Test
    @Sql("test_data.sql")
    @DisplayName("Test findByToken when token found then return optional of token")
    public void testFindByToken_whenSetValidToken_thenReturnOptionalOfToken() {
        String expectedToken = "USER2_TOKEN2";
        RefreshTokenRepository repository = new RefreshTokenRepositoryImpl(jdbcTemplate, new RefreshTokenRowMapper());
        Optional<RefreshToken> tokenOpt = repository.findByToken(expectedToken);
        Assertions.assertTrue(tokenOpt.isPresent());
        Assertions.assertEquals(expectedToken, tokenOpt.get().getToken());
    }

    @Test
    @Sql("test_data.sql")
    @DisplayName("Test findByToken when token found then return empty optional")
    public void testFindByToken_whenTokenFound_thenReturnEmptyOpt() {
        RefreshTokenRepository repository = new RefreshTokenRepositoryImpl(jdbcTemplate, new RefreshTokenRowMapper());
        Optional<RefreshToken> tokenOpt = repository.findByToken("NotFoundToken");
        Assertions.assertTrue(tokenOpt.isEmpty());
    }

    @Test
    @Sql("test_data.sql")
    public void testSaveIfTokenIsNull_thenThrowNewDataIntegrityException() {
        RefreshToken expectedRefreshToken = RefreshToken.builder()
                .userId(1L)
                .expireDate(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                .build();
        RefreshTokenRepository repository = new RefreshTokenRepositoryImpl(jdbcTemplate, new RefreshTokenRowMapper());
        Assertions.assertThrows(DataIntegrityViolationException.class ,() -> repository.save(expectedRefreshToken));
    }

    @Test
    @Sql("test_data.sql")
    @DisplayName("Test deleteAllById when method invoke delete all refresh tokens for specified userId")
    public void testDeleteAllByUserId_whenmethodInvoke_deleteAllTokensForUserId() {
        long expectedDeleteElementsCount = 2;
        RefreshTokenRepository repository = new RefreshTokenRepositoryImpl(jdbcTemplate, new RefreshTokenRowMapper());
        long beforeCount = repository.count();
        repository.deleteAllByUserId(2L);
        long afterCount = repository.count();
        Assertions.assertEquals(expectedDeleteElementsCount, beforeCount - afterCount);
    }

    @Test
    @Sql("test_data.sql")
    public void testDeleteAllByExpireDateLessThan_whenMethodInvoke_deleteAllTokensWhereExpireDataEndTimeLessThanNow() {
        long expectedCount = 1;
        RefreshTokenRepository repository = new RefreshTokenRepositoryImpl(jdbcTemplate, new RefreshTokenRowMapper());
        repository.deleteAllByExpireDateLessThan(Instant.now());
        long afterCount = repository.count();
        Assertions.assertEquals(expectedCount, afterCount);
    }

    @Test
    @Sql("test_data.sql")
    public void testSaveIfRefreshTokenValid_thenReturnSuccessResult() {
        RefreshToken expectedRefreshToken = RefreshToken.builder()
                .token("refresh_token")
                .userId(1L)
                .expireDate(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                .build();
        RefreshTokenRepository repository = new RefreshTokenRepositoryImpl(jdbcTemplate, new RefreshTokenRowMapper());

        RefreshToken refreshTokenFromDb = repository.save(expectedRefreshToken);

        Assertions.assertNotNull(refreshTokenFromDb.getId());
        Assertions.assertEquals(refreshTokenFromDb.getToken(), expectedRefreshToken.getToken());
        Assertions.assertEquals(refreshTokenFromDb.getUserId(), expectedRefreshToken.getUserId());
        Assertions.assertNotNull(refreshTokenFromDb.getExpireDate());
    }




}
