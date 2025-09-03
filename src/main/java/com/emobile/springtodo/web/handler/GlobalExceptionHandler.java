package com.emobile.springtodo.web.handler;

import com.emobile.springtodo.dto.response.ServiceMessage;
import com.emobile.springtodo.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений.
 * Предназначен для перехвата исключения и возвращения ответа с сообщением о возникшей ошибке клиенту,
 * в результате обработки запроса которого было выброшено исключение.
 * @author Aleksey Shvariov
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** Обработка исключения {@link RepositoryOperationException}
     * @param ex - перехваченное исключение {@link RepositoryOperationException}
     * @return возвращает сервисное сообщение о возникшей ошибке в виде объекта {@link ServiceMessage} */
    @ExceptionHandler(RepositoryOperationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ServiceMessage handeRepositoryOperationException(RepositoryOperationException ex) {
        final String message = "Внутренняя ошибка сервиса";
        log.error("{} : {}", message, ex.getMessage());
        return new ServiceMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }

    /** Обработка исключения {@link EntityIllegalStateException}
     * @param ex - перехваченное исключение {@link EntityIllegalStateException}
     * @return возвращает сервисное сообщение о возникшей ошибке в виде объекта {@link ServiceMessage} */
    @ExceptionHandler(EntityIllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ServiceMessage handeEntityIllegalStateException(EntityIllegalStateException ex) {
        final String message = "Внутренняя ошибка сервиса";
        log.error("{} : {}", message, ex.getMessage());
        return new ServiceMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }

    /** Обработка исключения {@link EntityNotFoundException}
     * @param ex - перехваченное исключение {@link EntityNotFoundException}
     * @return возвращает сервисное сообщение о возникшей ошибке в виде объекта {@link ServiceMessage} */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ServiceMessage handleEntityNotFoundException(EntityNotFoundException ex) {
        final String message = "Объект не найден";
        log.error("{} : {}", message, ex.getMessage());
        return new ServiceMessage(HttpStatus.NOT_FOUND.value(), message);
    }

    /** Обработка исключения {@link ServiceAuthenticationException}
     * @param ex - перехваченное исключение {@link ServiceAuthenticationException}
     * @return возвращает сервисное сообщение о возникшей ошибке в виде объекта {@link ServiceMessage} */
    @ExceptionHandler(ServiceAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ServiceMessage handleServiceAuthenticationException(ServiceAuthenticationException ex) {
        String message = "Ошибка авторизации: " + ex.getMessage();
        log.error("{} : {}", message, ex.getMessage());
        return new ServiceMessage(HttpStatus.UNAUTHORIZED.value(), message);
    }

    /** Обработка исключения {@link ConstraintViolationException},
     * возникающего при несоответствии данных указанных в запросе требованиям валидации.
     * @param ex - перехваченное исключение {@link ConstraintViolationException}
     * @return возвращает сервисное сообщение о возникшей ошибке в виде объекта {@link ServiceMessage}
     * */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceMessage handleConstrainViolationException(ConstraintViolationException ex) {
        log.error(ex.getMessage());
        return new ServiceMessage(HttpStatus.BAD_REQUEST.value(), selectOnlyMessage(ex.getMessage()));
    }

    /** Метод получения из сообщения, сформированного при выбрасывании исключения, информативную составляющую*/
    private String selectOnlyMessage(String messageSource) {
        String[] messageSourceParts = messageSource.split(": ");
        return messageSourceParts[messageSourceParts.length-1];
    }

    /** Обработка исключения {@link RefreshTokenExpirationException},
     * возникающего в случае получения в запросе просроченного токена.
     * @param ex - перехваченное исключение {@link RefreshTokenExpirationException}
     * @return возвращает сервисное сообщение о возникшей ошибке в виде объекта {@link ServiceMessage}
     */
    @ExceptionHandler(RefreshTokenExpirationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ServiceMessage handleRefreshTokenExpirationException(RefreshTokenExpirationException ex) {
        log.error(ex.getMessage());
        return new ServiceMessage(HttpStatus.UNAUTHORIZED.value(), selectOnlyMessage(ex.getMessage()));
    }

    /** Обработка исключения {@link MethodArgumentNotValidException},
     * возникающего при несоответствии данных требованиям валидации.
     * @param ex - перехваченное исключение {@link MethodArgumentNotValidException}
     * @return возвращает сервисное сообщение о возникшей ошибке в виде объекта {@link ServiceMessage}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceMessage handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage());
        return new ServiceMessage(HttpStatus.BAD_REQUEST.value(), parseMethodArgumentNotValidExceptionMessage(ex));
    }

    /** Метод получение сведений о возникшей ошибке из exception detailMessage.
     * @param ex - перехваченное исключение {@link MethodArgumentNotValidException}
     * @return возвращает сведения о возникшей ошибке в виде строки
     */
    private String parseMethodArgumentNotValidExceptionMessage(MethodArgumentNotValidException ex) {
        if (ex.getDetailMessageArguments().length == 0) {
            return ex.getMessage();
        }
        final int messageArgsLength = ex.getDetailMessageArguments().length;
        final String detailMessage = ex.getDetailMessageArguments()[messageArgsLength - 1].toString();
        return selectOnlyMessage(detailMessage);
    }

    /** Обработка исключения {@link UserOperationException},
     * возникающего при попытке нарушения пользователем прав доступа
     * (попытке получения или изменения данных, владельцем которых он не является).
     * @param ex - перехваченное исключение {@link UserOperationException}
     * @return возвращает сервисное сообщение о возникшей ошибке в виде объекта {@link ServiceMessage}
     */
    @ExceptionHandler(UserOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ServiceMessage handleUserOperationException(UserOperationException ex) {
        log.error(ex.getMessage());
        return new ServiceMessage(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    /** Обработка исключения {@link IllegalRequestArgumentsException},
     * возникающего при несоответствии данных указанных в запросе требованиям валидации.
     * (попытке получения или изменения данных, владельцем которых он не является).
     * @param ex - перехваченное исключение {@link IllegalRequestArgumentsException}
     * @return возвращает сервисное сообщение о возникшей ошибке в виде объекта {@link ServiceMessage}
     */
    @ExceptionHandler(IllegalRequestArgumentsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceMessage handleIllegalRequestArgumentsException(IllegalRequestArgumentsException ex) {
        log.error(ex.getMessage());
        return new ServiceMessage(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }





}
