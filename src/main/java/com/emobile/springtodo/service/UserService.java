package com.emobile.springtodo.service;

import com.emobile.springtodo.entity.ServiceUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    ServiceUser findById(Long userId);
    ServiceUser findByUsernameOrEmail(String usernameOrEmail);
    Page<ServiceUser> findAll(Pageable pageable);
    void saveUser(ServiceUser user);
    void deleteUserById(Long id);
}
