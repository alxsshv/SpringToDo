package com.emobile.springtodo.service;

import com.emobile.springtodo.dto.request.*;
import com.emobile.springtodo.dto.response.AuthResponse;
import com.emobile.springtodo.dto.response.RefreshTokenResponse;

/** Интерфейс, описывающий методы, определяющие логику подключения пользователей к сервису. */
public interface SecurityService {

    /** Метод регистрации пользователей
     * @param registerUserRequest - сведения о новом пользователе в виде объекта {@link RegisterUserRequest} */
    void registerUser(RegisterUserRequest registerUserRequest);

    /** Метод изменения данных своего аккаунта.
     * @param updateUserRequest - сведения об изменении данных аккаунта в виде объекта {@link UpdateUserRequest}
     * @param principalUsername - имя авторизованного пользователя, направившего запрос на изменение аккаунта. */
    void updateAccountByUser(UpdateUserRequest updateUserRequest, String principalUsername);

    /** Метод создания пользователя.
     * @param createUserRequest - сведения о новом пользователе в виде объекта {@link CreateUserRequest} */
    void createUser(CreateUserRequest createUserRequest);

    /** Метод обновления данных пользователя.
     * @param updateUserRequest - сведения о пользователе, содаржащие изменённую информацию,
     *                         в виде объекта {@link UpdateUserRequest}..
     * @param id - уникальный идентификатор пользователя, данные которого планируется изменить. */
    void updateUser(UpdateUserRequest updateUserRequest, Long id);

    /** Метод подключения пользователя к сервису с применением аутентификации по логину и паролю.
     * @param loginRequest - данные для подключения пользователя к сервису в виде объекта {@link LoginRequest}
     * @return возвращает ответ в виде объекта {@link AuthResponse}*/
    AuthResponse loginUser(LoginRequest loginRequest);

    /** Метод обновления refresh-токена и access-токена по ранее выданному refresh-токену.
     * @param refreshTokenRequest - ранее выданный refresh-токен в виде объекта {@link RefreshTokenRequest}
     * @return - возвращает ответ в виде объекта {@link RefreshTokenResponse}*/
    RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    /** Метод отключения пользователя от сервиса (выхода из сервиса)
     * @param userId  - уникальный идентификатор пользователя. */
    void logout(long userId);
}
