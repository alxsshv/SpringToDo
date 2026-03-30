package com.emobile.springtodo.service.impl;

import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.exception.EntityNotFoundException;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Сервис для управления пользователями. */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** Репозиторий для хранения пользователей в БД. */
    private final UserRepository userRepository;

    /** Метод поиска пользователя по уникальному идентификатору.
     * @param userId  - уникальный идентификатор пользователя.
     * @return возвращает объект {@link ServiceUser}
     * @throws com.emobile.springtodo.exception.EntityNotFoundException будет выброшено,
     * если пользователь не найден.*/
    @Override
    public ServiceUser findById(Long userId) {
        Optional<ServiceUser> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("Пользователь с id = " + userId + " не найден");
        }
        return userOpt.get();
    }

    /** Метод поиска пользователя по имени пользователя или адресу электронной почты.
     * @param usernameOrEmail - имя пользователя или адреса электронной почты.
     * @return возвращает объект {@link ServiceUser}
     * @throws com.emobile.springtodo.exception.EntityNotFoundException будет выброшено,
     * если пользователь не найден.*/
    @Override
    public ServiceUser findByUsernameOrEmail(String usernameOrEmail) {
        Optional<ServiceUser> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("Пользователь " + usernameOrEmail + " не найден");
        }
        return userOpt.get();
    }

    /** Метод постраничного получения данных о пользователях.
     * @param pageable - параметры пагинации в виде объекта {@link Pageable}
     * @return возвращает страницу с объектами {@link ServiceUser}
     */
    @Override
    public Page<ServiceUser> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /** Метод сохранения пользователя.
     * @param user - сведения о вновь создаваемом пользователе в виде {@link ServiceUser}
     */
    @Override
    public void saveUser(ServiceUser user) {
        userRepository.save(user);
    }

    /** Метод удаления пользователя по уникальному идентификатору.
     * @param id - уникальный идентификатор удаляемого пользователя.
     * */
    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}


