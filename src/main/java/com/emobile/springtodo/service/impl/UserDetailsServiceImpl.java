package com.emobile.springtodo.service.impl;

import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.exception.EntityNotFoundException;
import com.emobile.springtodo.security.AppUserDetails;
import com.emobile.springtodo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Реализация интерфейса {@link UserDetailsService} для использования при аутентификации пользователя. */
@Service
@Primary
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /** Сервис для управления пользователями */
    private final UserService userService;

    /** Метод получения объекта {@link AppUserDetails} по имени пользователя или адресу электронной почты.
     * @param usernameOrEmail - имя пользователя или адрес электронной почты
     * @return экземпляр класса {@link AppUserDetails}.
     * @throws UsernameNotFoundException если пользователь с указанным
     * именем пользователя или адресом электронной почты не найден.*/
    @Override
    public AppUserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        ServiceUser user;
        try {
           user = userService.findByUsernameOrEmail(usernameOrEmail);
        } catch (EntityNotFoundException ex) {
            throw new UsernameNotFoundException(ex.getMessage());
        }
        return new AppUserDetails(user);
    }
}
