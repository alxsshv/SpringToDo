package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.RefreshToken;

import java.time.Instant;
import java.util.Optional;


/** Интерфейс для описания методов выполнения CRUD-операций в БД с сущностью {@link RefreshToken} */
public interface RefreshTokenRepository extends EntityRepository<RefreshToken, Long> {

    /** Метод сохранения и обновления токена в БД. Если переданный в качестве параметра
     *  объект не имеет идентификатора (id = null), создается новая запись в БД.
     *  Если переданный в качестве параметра объект имеет уникальный идентификатор,
     *  то обновляется уже существующая запись в БД.
     * @param refreshToken  - сохраняемый экземпляр класса {@link RefreshToken}
     * @return возвращает сохранённую (обновлённую) запись в БД в виде объекта {@link RefreshToken}*/
    RefreshToken save(RefreshToken refreshToken);

    /**
     * Метод получения данных о refresh-токене по токену.
     * @param token - значение токена для поиска записи об объекте {@link RefreshToken}.
     * @return возвращает {@link Optional} с объектом класса {@link RefreshToken},
     * или пустой Optional если запись об объекте не найдена.
     */
    Optional<RefreshToken> findByToken(String token);


    /** Удаление всех записей о refresh-токенах пользователя с указанным идентификатором.
     * @param userId - уникальный идентификатор пользователя.*/
    void deleteAllByUserId(long userId);

    /** Удаление всех записей о refresh-токенах срок действия которых (expireDate) меньше указанного значения.
     * @param now  - временная граница, для удаления токенов срок действия которых закончился ранее установленной границы.
     */
    void deleteAllByExpireDateLessThan(Instant now);

    /** Удаление записи по уникальному идентификатору.
     * @param id - уникальный идентификатор токена, который необходимо удалить*/
    void deleteById(Long id);

    /** Счётчик количества записей в таблице.
     * Возвращает количество записей в таблице в формате Long. */
    Long count();
}
