package com.emobile.springtodo.service.impl;

import com.emobile.springtodo.dto.mapper.ServiceUserMapper;
import com.emobile.springtodo.dto.request.*;
import com.emobile.springtodo.dto.response.AuthResponse;
import com.emobile.springtodo.dto.response.RefreshTokenResponse;
import com.emobile.springtodo.entity.RefreshToken;
import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.exception.EntityNotFoundException;
import com.emobile.springtodo.exception.IllegalRequestArgumentsException;
import com.emobile.springtodo.exception.ServiceAuthenticationException;
import com.emobile.springtodo.exception.UserOperationException;
import com.emobile.springtodo.security.AppUserDetails;
import com.emobile.springtodo.security.SecurityRole;
import com.emobile.springtodo.service.RefreshTokenService;
import com.emobile.springtodo.service.SecurityService;
import com.emobile.springtodo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private  ServiceUserMapper serviceUserMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void registerUser(RegisterUserRequest registerUserRequest) {
        ServiceUser user = serviceUserMapper.mapFrom(registerUserRequest);
        user.setRoles(Collections.singleton(SecurityRole.ROLE_USER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);
    }

    @Override
    public void updateAccountByUser(UpdateUserRequest updateUserRequest, String principalUsername) {
        if (!updateUserRequest.getUsername().equals(principalUsername)) {
            throw new UserOperationException("Неверно указано имя пользователя. " +
                    "Имя пользователя не доступно для изменения. Пользователь может изменять данные только своего аккаунта.");
        }
        final ServiceUser user = userService.findByUsernameOrEmail(principalUsername);
        checkUpdateUserParams(updateUserRequest, user.getId(), user);
        user.setEmail(updateUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        userService.saveUser(user);
    }

    @Override
    public void createUser(CreateUserRequest createUserRequest) {
        final ServiceUser user = serviceUserMapper.mapFrom(createUserRequest);
        user.setRoles(SecurityRole.valuesOfNames(createUserRequest.getRoleNames()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);
    }

    @Override
    public void updateUser(UpdateUserRequest updateUserRequest, Long id) {
        final ServiceUser user = userService.findByUsernameOrEmail(updateUserRequest.getUsername());
        checkUpdateUserParams(updateUserRequest, id, user);
        user.setEmail(updateUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        user.setRoles(SecurityRole.valuesOfNames(updateUserRequest.getRoleNames()));
        userService.saveUser(user);
    }

    private void checkUpdateUserParams(UpdateUserRequest updateUserRequest,
                                       Long requestParamId,
                                       ServiceUser userFromDb) {
        if (!userFromDb.getId().equals(requestParamId)) {
            throw new IllegalRequestArgumentsException("Неверно указан id для пользователя " + userFromDb.getUsername());
        }
        if (!isThisUserEmailOrEmailNotExist(userFromDb, updateUserRequest.getEmail())) {
            throw new IllegalRequestArgumentsException("Адрес электронной почты "
                    + updateUserRequest.getEmail() + " занят другим пользователем");
        }
    }

    private boolean isThisUserEmailOrEmailNotExist(ServiceUser user, String email) {
        ServiceUser emailOwner;
        try {
            emailOwner = userService.findByUsernameOrEmail(email);
        } catch (EntityNotFoundException ex) {
            return true;
        }
        return emailOwner.getUsername().equals(user.getUsername());
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {
        final Authentication authentication = getAuthentication(loginRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return buildAuthResponse(authentication);
    }

    private Authentication getAuthentication(LoginRequest loginRequest) {
        try {
            ServiceUser user = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),loginRequest.getPassword()));
        } catch (EntityNotFoundException ex) {
            throw new ServiceAuthenticationException(ex.getMessage());
        }
    }

    private AuthResponse buildAuthResponse(Authentication authentication) {
        final AppUserDetails appUserDetails = (AppUserDetails) authentication.getPrincipal();
        final List<String> roleNames = appUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        final RefreshToken refreshToken = refreshTokenService.generateRefreshToken(appUserDetails.getId());
        final String accessToken = refreshTokenService.generateAccessToken(appUserDetails.getUsername());
        return AuthResponse.builder()
                .id(appUserDetails.getId())
                .username(appUserDetails.getUsername())
                .email(appUserDetails.getEmail())
                .roles(roleNames)
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        final RefreshToken oldRefreshToken = refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken());
        refreshTokenService.check(oldRefreshToken);
        final ServiceUser user = userService.findById(oldRefreshToken.getUserId());
        final String accessToken = refreshTokenService.generateAccessToken(user.getUsername());
        final String refreshToken = refreshTokenService.generateRefreshToken(user.getId()).getToken();
        return new RefreshTokenResponse(refreshToken, accessToken);
    }

    @Override
    public void logout(long userId) {
        refreshTokenService.deleteByUserId(userId);
    }
}
