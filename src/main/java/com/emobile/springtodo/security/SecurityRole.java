package com.emobile.springtodo.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum SecurityRole {
    ROLE_USER,
    ROLE_ADMIN;


    public static Set<SecurityRole> valuesOfNames(Set<String> roleName) {
        return roleName.stream()
                .filter(SecurityRole::isValidRole)
                .map(SecurityRole::valueOf)
                .collect(Collectors.toSet());
    }

    public static boolean isValidRole(String roleName) {
        return Arrays.stream(SecurityRole.values())
                .map(SecurityRole::name)
                .anyMatch(name -> name.equals(roleName));
    }
}
