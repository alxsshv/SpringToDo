package com.emobile.springtodo.security;

import com.emobile.springtodo.entity.ServiceUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/** Реализация интерфейса {@link UserDetails}*/
@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {

    /** Пользователь, для которого используется реализация UserDetails */
    private final ServiceUser user;

    /** Метод получения идентификатора пользователя.
     * @return возвращает уникальный идентификатор пользователя */
    public Long getId(){
        return user.getId();
    }

    /** Метод получения прав пользователя (ролей).
     * @return возвращает коллекцию ролей пользователя.*/
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
    }

    /** Метод получения пароля пользователя
     * @return возвращает пароль пользователя */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /** Метод получения имени пользователя.
     * @return возвращает имя пользователя */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /** Метод получения адреса электронной почты пользователя
     * @return возвращает адрес электронной почты пользователя */
    public String getEmail() {
        return user.getEmail();
    }

    /** Метод получения строкового представления объекта AppUserDetails. */
    @Override
    public String toString() {
        return "AppUserDetails{" +
                "user=" + user.toString() +
                '}';
    }
}
