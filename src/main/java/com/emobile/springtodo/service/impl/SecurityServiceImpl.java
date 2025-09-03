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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/** Сервис, реализующий логику подключения пользователей. */
@Service
@Slf4j
public class SecurityServiceImpl implements SecurityService {

    /** Маппер для преобразования пользователей в объекты передачи данных и наоборот */
    @Autowired
    private  ServiceUserMapper serviceUserMapper;

    /** Менеджер аутентификации, реализующий логику аутентификации пользователя */
    @Autowired
    private AuthenticationManager authenticationManager;

    /** Сервис управления пользователями */
    @Autowired
    private UserService userService;

    /** Сервис управления токенами */
    @Autowired
    private RefreshTokenService refreshTokenService;

    /** Объект для шифрования паролей пользователей. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Метод регистрации пользователей
     * @param registerUserRequest - сведения о новом пользователе в виде объекта {@link RegisterUserRequest} */
    @Override
    @Transactional
    public void registerUser(RegisterUserRequest registerUserRequest) {
        ServiceUser user = serviceUserMapper.mapFrom(registerUserRequest);
        user.setRoles(Collections.singleton(SecurityRole.ROLE_USER));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);
    }

    /** Метод изменения данных своего аккаунта.
     * @param updateUserRequest - сведения об изменении данных аккаунта в виде объекта {@link UpdateUserRequest}
     * @param principalUsername - имя авторизованного пользователя, направившего запрос на изменение аккаунта. */
    @Override
    @Transactional
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

    /** Метод создания пользователя.
     * @param createUserRequest - сведения о новом пользователе в виде объекта {@link CreateUserRequest} */
    @Override
    @Transactional
    public void createUser(CreateUserRequest createUserRequest) {
        final ServiceUser user = serviceUserMapper.mapFrom(createUserRequest);
        user.setRoles(SecurityRole.valuesOfNames(createUserRequest.getRoleNames()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);
    }

    /** Метод обновления данных пользователя.
     * @param updateUserRequest - сведения о пользователе, содаржащие изменённую информацию,
     *                         в виде объекта {@link UpdateUserRequest}..
     * @param id - уникальный идентификатор пользователя, данные которого планируется изменить. */
    @Override
    @Transactional
    public void updateUser(UpdateUserRequest updateUserRequest, Long id) {
        final ServiceUser user = userService.findByUsernameOrEmail(updateUserRequest.getUsername());
        checkUpdateUserParams(updateUserRequest, id, user);
        user.setEmail(updateUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        user.setRoles(SecurityRole.valuesOfNames(updateUserRequest.getRoleNames()));
        userService.saveUser(user);
    }

    /** Метод проверки пригодности значений полей объекта {@link UpdateUserRequest} к обновлению данных пользователя.
     * @param updateUserRequest - сведения о пользователе для обновления.
     * @param requestParamId  - уникальный идентификатор пользователя из запроса на обновления пользователя.
     * @param userFromDb - пользователь, сохраённый в БД.
     * @throws IllegalRequestArgumentsException - будет выброшено если параметры запроса не позволяют
     * выполнить изменение данных пользователя.
     * */
    private void checkUpdateUserParams(UpdateUserRequest updateUserRequest,
                                       Long requestParamId,
                                       ServiceUser userFromDb) {
        if (!userFromDb.getId().equals(requestParamId)) {
            String errorMessage = "Неверно указан id для пользователя " + userFromDb.getUsername();
            log.error(errorMessage);
            throw new IllegalRequestArgumentsException(errorMessage);
        }
        if (!isThisUserEmailOrEmailNotExist(userFromDb, updateUserRequest.getEmail())) {
            String errorMessage = "Адрес электронной почты "
                    + updateUserRequest.getEmail() + " занят другим пользователем";
            log.info(errorMessage);
            throw new IllegalRequestArgumentsException(errorMessage);
        }
    }

    /** Метод проверяет, что указанный адрес электронной почты принадлежит данному пользователю и
     *  не используется другими пользователями.
     *  @param user  - пользователь, для которого планируется использовать адрес электронной почты.
     *  @param email - адрес электронной почты.
     *  @return возвращает true если адрес электронной почты принадлежит данному пользователю
     *  или не используется другими пользователями,
     *  или возвращает false если адрес электронной почты занят другим пользователем.*/
    private boolean isThisUserEmailOrEmailNotExist(ServiceUser user, String email) {
        ServiceUser emailOwner;
        try {
            emailOwner = userService.findByUsernameOrEmail(email);
        } catch (EntityNotFoundException ex) {
            return true;
        }
        return emailOwner.getUsername().equals(user.getUsername());
    }

    /** Метод подключения пользователя к сервису с применением аутентификации по логину и паролю.
     * @param loginRequest - данные для подключения пользователя к сервису в виде объекта {@link LoginRequest}
     * @return возвращает ответ в виде объекта {@link AuthResponse}*/
    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {
        final Authentication authentication = getAuthentication(loginRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return buildAuthResponse(authentication);
    }

    /** Метод получения объекта {@link Authentication} по имения пользователя и паролю,
     *  представленными в виде объекта {@link LoginRequest}
     *  @param loginRequest - объект {@link LoginRequest}, содержащий имя пользователя и пароль пользователя
     *  @return возвращает объект {@link Authentication}.
     *  @throws ServiceAuthenticationException будет выброшено если не удалось аутентифицировать пользователя.*/
    private Authentication getAuthentication(LoginRequest loginRequest) {
        try {
            ServiceUser user = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),loginRequest.getPassword()));
        } catch (EntityNotFoundException ex) {
            throw new ServiceAuthenticationException(ex.getMessage());
        }
    }

    /** Метод формирования объекта {@link AuthResponse} нв основе данных аутентификации пользователя.
     * @param authentication - сведения об аутентифицированном пользователе.
     * @return возвращает ответ на запрос в формате {@link AuthResponse}*/
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

    /** Метод обновления refresh-токена и access-токена по ранее выданному refresh-токену.
     * @param refreshTokenRequest - ранее выданный refresh-токен в виде объекта {@link RefreshTokenRequest}
     * @return - возвращает ответ в виде объекта {@link RefreshTokenResponse}*/
    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        final RefreshToken oldRefreshToken = refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken());
        refreshTokenService.validate(oldRefreshToken);
        final ServiceUser user = userService.findById(oldRefreshToken.getUserId());
        final String accessToken = refreshTokenService.generateAccessToken(user.getUsername());
        final String refreshToken = refreshTokenService.generateRefreshToken(user.getId()).getToken();
        return new RefreshTokenResponse(refreshToken, accessToken);
    }

    /** Метод отключения пользователя от сервиса (выхода из сервиса)
     * @param userId  - уникальный идентификатор пользователя. */
    @Override
    public void logout(long userId) {
        refreshTokenService.deleteByUserId(userId);
    }
}
