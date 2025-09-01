package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.ServiceUser;

import java.util.Optional;

public interface UserRepository extends EntityRepository<ServiceUser, Long>{
    Optional<ServiceUser> findByUsernameOrEmail(String username, String email);
    boolean existByEmail(String email);
    boolean existByUsername(String username);
}
