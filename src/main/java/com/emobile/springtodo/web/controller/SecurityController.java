package com.emobile.springtodo.web.controller;

import com.emobile.springtodo.dto.request.LoginRequest;
import com.emobile.springtodo.dto.request.RefreshTokenRequest;
import com.emobile.springtodo.dto.request.RegisterUserRequest;
import com.emobile.springtodo.dto.response.AuthResponse;
import com.emobile.springtodo.dto.response.RefreshTokenResponse;
import com.emobile.springtodo.dto.response.ServiceMessage;
import com.emobile.springtodo.security.AppUserDetails;
import com.emobile.springtodo.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер, для обработки запросов, связанных с авторизацией пользователей
 * @author Aleksey Shvariov
 */
@Tag(name = "Контроллер безопасности приложения",
        description = "Обработка запросов, связанных с авторизацией пользователей")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Slf4j
public class SecurityController {

    /** Сервис, реализующий логику авторизации пользователей */
    private final SecurityService securityService;

    /** Метод обработки запросов на подключение пользователя к сервису
     *  с помощью методов базовой аутентификации (по логину и паролю).
     *  @param request - тело запроса на подключение к сервису в виде объекта {@link ServiceMessage}
     *  @return возвращает сведения об аутентифицированном пользователе в виде объекта {@link AuthResponse}
     */
    @Operation(summary = "Подключение пользователя к системе")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        final AuthResponse authResponse = securityService.loginUser(request);
        log.info("Пользователь {} подключился к сервису", authResponse.getUsername());
        return authResponse;
    }

    /** Метод обработки запросов на регистрацию пользователей.
     * @param request - тело запроса, содержащее необходимые сведения
     *               на регистрацию пользователя в виде объекта {@link RegisterUserRequest}.
     * @return возвращает сообщение об успешной регистрации пользователя в виде объекта {@link ServiceMessage}
     */
    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public ServiceMessage register(@RequestBody @Valid RegisterUserRequest request) {
        securityService.registerUser(request);
        final String message = String.format("Пользователь %s успешно зарегистрирован", request.getUsername());
        log.info(message);
        return new ServiceMessage(HttpStatus.OK.value(), message);
    }

    /** Метод обработки запросов на обновление jwt-токена.
     * @param request - тело запроса, содержащее выданный пользователю
     *               RefreshToken в виде объекта {@link RefreshTokenRequest}.
     * @return возвращает новую пару токенов (refresh-токен и access-токен) в виде объекта {@link RefreshTokenResponse}
     * */
    @Operation(summary = "Обновление jwt-токена")
    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public RefreshTokenResponse refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return securityService.refreshToken(request);
    }


    /** Отключение пользователя от сервиса.
     * @return возвращает сообщение об успешном выходе пользователя из сервиса */
    @Operation(summary = "Выход пользователя из аккаунта сервиса")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ServiceMessage logout(@AuthenticationPrincipal AppUserDetails userDetails) {
        securityService.logout(userDetails.getId());
        final String message = String.format("Пользовать %s завершил работу с сервисом", userDetails.getUsername());
        log.info(message);
        return new ServiceMessage(HttpStatus.OK.value(), message);
    }

}
