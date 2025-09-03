package com.emobile.springtodo.service;

import com.emobile.springtodo.entity.ServiceUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Интерфейс, описывающий методы работы с пользователями */
public interface UserService {

    /** Метод поиска пользователя по уникальному идентификатору.
     * @param userId  - уникальный идентификатор пользователя.
     * @return возвращает объект {@link ServiceUser}
     * @throws com.emobile.springtodo.exception.EntityNotFoundException будет выброшено,
     * если пользователь не найден.*/
    ServiceUser findById(Long userId);

    /** Метод поиска пользователя по имени пользователя или адресу электронной почты.
     * @param usernameOrEmail - имя пользователя или адреса электронной почты.
     * @return возвращает объект {@link ServiceUser}
     * @throws com.emobile.springtodo.exception.EntityNotFoundException будет выброшено,
     * если пользователь не найден.*/
    ServiceUser findByUsernameOrEmail(String usernameOrEmail);

    /** Метод постраничного получения данных о пользователях.
     * @param pageable - параметры пагинации в виде объекта {@link Pageable}
     * @return возвращает страницу с объектами {@link ServiceUser}
     */
    Page<ServiceUser> findAll(Pageable pageable);

    /** Метод сохранения пользователя.
     * @param user - сведения о вновь создаваемом пользователе в виде {@link ServiceUser}
     */
    void saveUser(ServiceUser user);

    /** Метод удаления пользователя по уникальному идентификатору.
     * @param id - уникальный идентификатор удаляемого пользователя. */
    void deleteUserById(Long id);
}
