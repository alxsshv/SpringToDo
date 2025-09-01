package com.emobile.springtodo.entity;

import com.emobile.springtodo.repository.annotation.TableName;
import com.emobile.springtodo.security.SecurityRole;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Пользователь сервиса
 * @author Alexei Shvariov
 */

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("service_users")
public class ServiceUser implements Identifiable<Long> {
    /** Имя таблицы для хранения ролей в БД */
    public final static String roleTableName = "security_roles";

    /** Идентификатор пользователя*/
    private Long id;

    /** Имя пользователя */
    private String username;

    /** Адрес электронной почты */
    private String email;

    /** Пароль пользователя */
    @ToString.Exclude
    private transient String password;

    /** Роли пользователя */
    private Set<SecurityRole> roles = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServiceUser that = (ServiceUser) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email);
    }

    public static String getRoleTableName() {
        return ServiceUser.roleTableName;
    }
}
