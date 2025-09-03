package com.emobile.springtodo.service;

import com.emobile.springtodo.entity.RefreshToken;

/** Интерфейс, описывающий методы работы с refresh-токенами. */
public interface RefreshTokenService {

    /** Метод поиска refresh-токена по токену.
     * @param token - строка-токен
     * @return возвращает экземпляр класса {@link RefreshToken}
     * @throws com.emobile.springtodo.exception.EntityNotFoundException, если токен не найден.*/
    RefreshToken findByToken(String token);

    /** Метод создания нового refresh-токена для пользователя
     * с указанным в качестве параметра идентификатором.
     * @param userId - уникальный идентификатор пользователя.
     * @return возвращает экземпляр класса {@link RefreshToken} */
    RefreshToken generateRefreshToken(long userId);

    /** Метод создания нового access-токена по имени пользователя.
     * @param username - имя пользователя, для которого создается access-токен.
     * @return возвращает access-токен в виде строки. */
    String generateAccessToken(String username);

    /** Метод проверки валидности refresh-токена.
     * @param refreshToken - проверяемый экземпляр класса {@link RefreshToken}
     * @throws com.emobile.springtodo.exception.RefreshTokenExpirationException, если токен просрочен.*/
    void validate(RefreshToken refreshToken);

    /** Метод удаления resfresh-токенов пользователя.
     * @param userId - уникальный идентификатор пользователя.*/
    void deleteByUserId(long userId);

    /** Метод удаления токенов с истекшим сроком действия.*/
    void deleteExpiredToken();

}
