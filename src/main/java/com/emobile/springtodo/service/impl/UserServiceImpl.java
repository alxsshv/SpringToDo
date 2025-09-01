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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public ServiceUser findById(Long userId) {
        Optional<ServiceUser> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("Пользователь с id = " + userId + " не найден");
        }
        return userOpt.get();
    }

    @Override
    public ServiceUser findByUsernameOrEmail(String usernameOrEmail) {
        Optional<ServiceUser> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("Пользователь " + usernameOrEmail + " не найден");
        }
        return userOpt.get();
    }

    @Override
    public Page<ServiceUser> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public void saveUser(ServiceUser user) {
        userRepository.save(user);
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}


