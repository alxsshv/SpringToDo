package com.emobile.springtodo.service.impl;

import com.emobile.springtodo.entity.RefreshToken;
import com.emobile.springtodo.exception.EntityNotFoundException;
import com.emobile.springtodo.exception.RefreshTokenExpirationException;
import com.emobile.springtodo.repository.RefreshTokenRepository;
import com.emobile.springtodo.security.jwt.JwtUtils;
import com.emobile.springtodo.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Сервис, описывающий методы работы с refresh-токенами. */
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    /** Срок действия refresh-токена */
    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    /** Репозиторий для взаимодействия с БД в части хранения refresh-токенов */
    private final RefreshTokenRepository refreshTokenRepository;

    /** Утилитарный класс, содержащий методы генерации получения информации из jwt-токенов. */
    private final JwtUtils jwtUtils;

    /** Метод поиска refresh-токена по токену.
     * @param token - строка-токен
     * @return возвращает экземпляр класса {@link RefreshToken}
     * @throws com.emobile.springtodo.exception.EntityNotFoundException, если токен не найден.*/
    @Override
    public RefreshToken findByToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            throw new EntityNotFoundException("Refresh токен не найден");
        }
        return refreshTokenOpt.get();
    }

    /** Метод создания нового refresh-токена для пользователя
     * с указанным в качестве параметра идентификатором.
     * @param userId - уникальный идентификатор пользователя.
     * @return возвращает экземпляр класса {@link RefreshToken} */
    @Override
    @Transactional
    public RefreshToken generateRefreshToken(long userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expireDate(Instant.now().plusSeconds(refreshTokenExpiration.toSeconds()))
                .build();
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    /** Метод создания нового access-токена по имени пользователя.
     * @param username - имя пользователя, для которого создается access-токен.
     * @return возвращает access-токен в виде строки. */
    @Override
    public String generateAccessToken(String username) {
        return jwtUtils.generateAccessToken(username);
    }

    /** Метод проверки валидности refresh-токена.
     * @param refreshToken - проверяемый экземпляр класса {@link RefreshToken}
     * @throws com.emobile.springtodo.exception.RefreshTokenExpirationException, если токен просрочен.*/
    @Override
    public void validate(RefreshToken refreshToken) {
        if (refreshToken.getExpireDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.deleteById(refreshToken.getId());
            throw new RefreshTokenExpirationException("Срок действия токена "
                    + refreshToken.getToken() + " истек. Пожалуйста авторизуйтесь при помощи логина и пароля");
        }
    }

    /** Метод удаления resfresh-токенов пользователя.
     * @param userId - уникальный идентификатор пользователя.*/
    @Override
    public void deleteByUserId(long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    /** Метод удаления токенов с истекшим сроком действия.*/
    @Override
    public void deleteExpiredToken() {
        refreshTokenRepository.deleteAllByExpireDateLessThan(Instant.now());
    }
}
