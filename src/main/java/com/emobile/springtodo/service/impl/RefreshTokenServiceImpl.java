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

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtils jwtUtils;

    @Override
    public RefreshToken findByToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            throw new EntityNotFoundException("Refresh токен не найден");
        }
        return refreshTokenOpt.get();
    }

    @Override
    public RefreshToken generateRefreshToken(long userId) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expireDate(Instant.now().plusSeconds(refreshTokenExpiration.toSeconds()))
                .build();
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Override
    public String generateAccessToken(String username) {
        return jwtUtils.generateAccessToken(username);
    }

    @Override
    public void check(RefreshToken refreshToken) {
        if (refreshToken.getExpireDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.deleteById(refreshToken.getId());
            throw new RefreshTokenExpirationException("Срок действия токена "
                    + refreshToken.getToken() + " истек. Пожалуйста авторизуйтесь при помощи логина и пароля");
        }
    }

    @Override
    public void deleteByUserId(long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    @Override
    public void deleteExpiredToken() {
        refreshTokenRepository.deleteAllByExpireDateLessThan(Instant.now());
    }
}
