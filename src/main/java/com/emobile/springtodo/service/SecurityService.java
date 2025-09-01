package com.emobile.springtodo.service;

import com.emobile.springtodo.dto.request.*;
import com.emobile.springtodo.dto.response.AuthResponse;
import com.emobile.springtodo.dto.response.RefreshTokenResponse;


public interface SecurityService {
    void registerUser(RegisterUserRequest registerUserRequest);
    void updateAccountByUser(UpdateUserRequest updateUserRequest, String principalUsername);
    void createUser(CreateUserRequest createUserRequest);
    void updateUser(UpdateUserRequest updateUserRequest, Long id);
    AuthResponse loginUser(LoginRequest loginRequest);
    RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    void logout(long userId);
}
