package com.emobile.springtodo.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Класс-перечисление, содержащее допустимые роли пользователя */
public enum SecurityRole {
    ROLE_USER,
    ROLE_ADMIN;

    /** Метод получения набора ролей пользователя из их строкового представления.
     * @param roleName - напор имен ролей пользователя.
     * @return возвращает набор (Set) ролей пользователя.*/
    public static Set<SecurityRole> valuesOfNames(Set<String> roleName) {
        return roleName.stream()
                .filter(SecurityRole::isValidRole)
                .map(SecurityRole::valueOf)
                .collect(Collectors.toSet());
    }

    /** Метод проверки возможности преобразования переданной в качестве параметра строки
     * в объект класса SecurityRole.
     * @param roleName - название роли пользователя.
     * @return возвращает true - если из строки можно получить роль пользователя,
     * или false - если строка не пригодна для получения роли пользователя.*/
    public static boolean isValidRole(String roleName) {
        return Arrays.stream(SecurityRole.values())
                .map(SecurityRole::name)
                .anyMatch(name -> name.equals(roleName));
    }
}
