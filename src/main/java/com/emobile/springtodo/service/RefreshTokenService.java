package com.emobile.springtodo.service;

import com.emobile.springtodo.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken findByToken(String token);
    RefreshToken generateRefreshToken(long userId);
    String generateAccessToken(String username);
    void check(RefreshToken refreshToken);
    void deleteByUserId(long userId);
    void deleteExpiredToken();
}
