package com.emobile.springtodo.web.controller;

import com.emobile.springtodo.configuration.AppDefaults;
import com.emobile.springtodo.dto.mapper.ServiceUserMapper;
import com.emobile.springtodo.dto.request.CreateUserRequest;
import com.emobile.springtodo.dto.request.UpdateUserRequest;
import com.emobile.springtodo.dto.response.ServiceMessage;
import com.emobile.springtodo.dto.response.ServiceUserResponse;
import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.service.SecurityService;
import com.emobile.springtodo.service.UserService;
import com.emobile.springtodo.validation.IsUserSortProperty;
import com.emobile.springtodo.validation.IsValidDirection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер, обрабатывающий запросы CRUD операции с пользователями {@link ServiceUser}
 * @author Aleksey Shvariov
 */

@Tag(name = "Контроллер управления пользователями",
        description = "Контроллер, обрабатывающий запросы на создание," +
                " изменение, получение информации о пользователях и их удаление")
@Validated
@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    /** Сервис, реализующей логику авторизации пользователей*/
    private final SecurityService securityService;

    /** Сервис, реализующий логику получения информации о пользователях,
     *  создания изменения и удаления записей о пользователях*/
    private final UserService userService;

    /** Маппер для преобразования объектов передачи данных запросов о пользователях в сущность {@link ServiceUser}
     *  и преобразования сущности {@link ServiceUser} в объекты передачи данных*/
    private final ServiceUserMapper serviceUserMapper;


    /** Метод обработки запроса на создание нового пользователя. Требуется роль администратора.
     * @param request - тело запроса на создание пользователя, хранящее необходимую информацию о создаваемом
     *               пользователе в виде объекта передачи данных {@link CreateUserRequest}.
     * @return сообщение об успешном создании пользователя в виде объекта {@link ServiceMessage}*/
    @Operation(summary = "Создание нового пользователя")
    @SecurityRequirement(name = "JWT")
    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceMessage createUser(@RequestBody @Valid CreateUserRequest request) {
        securityService.createUser(request);
        String message = String.format("Создан новый пользователь %s", request.getUsername());
        log.info(message);
        return new ServiceMessage(HttpStatus.CREATED.value(), message);
    }

    /** Метод обработки запроса на получение постраничное получение списка пользователей. Требуется роль администратора.
     * @param size - целочисленное значение количества записей на странице (по умолчанию - 10).
     * @param pageNumber - номер станицы, которую необходимо загрузить (по умолчанию - 0).
     * @param dir - направление сортировки ASC (по возрастанию) или DESC (по убыванию).
     * @param sortProperty - свойство пользователя по которому необходимо выполнять сортировку (по умолчанию id);
     * @return возвращает страницу с объектами передачи данных {@link ServiceUserResponse}
     */
    @Operation(summary = "Получение постраничного списка пользователей")
    @SecurityRequirement(name = "JWT")
    @GetMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public PagedModel<ServiceUserResponse> getAllUsers(
            @RequestParam(value = "size", defaultValue = AppDefaults.PAGE_SIZE)
            @Min(value = 1, message = "Размер страницы не может быть меньше нуля")
            @Max(value = 100, message = "Размер страницы не должен превышать 100 записей") int size,
            @RequestParam(value = "page", defaultValue = AppDefaults.PAGE_NUMBER)
            @Min(value = 0, message = "Некорректное значение номера страницы") int pageNumber,
            @RequestParam(value = "dir", defaultValue = AppDefaults.PAGE_DIRECTION)
            @IsValidDirection String dir,
            @RequestParam(value = "sortBy", defaultValue = AppDefaults.PAGE_SORT_PROP)
            @IsUserSortProperty String sortProperty) {
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.valueOf(dir.toUpperCase()), sortProperty));
        Page<ServiceUserResponse> users = userService.findAll(pageable).map(serviceUserMapper::mapToServiceUserResponse);
        return new PagedModel<>(users);
    }


    /** Метод обработки запроса на получение информации о пользователе по уникальному идентификатору. Требуется роль администратора.
     * @param id - уникальный идентификатор пользователя
     * @return возвращает информацию о пользователе в виде объекта передачи данных {@link ServiceUserResponse}*/
    @Operation(summary = "Получение информации о пользователе по уникальному идентификатору")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public ServiceUserResponse getUserById(@PathVariable("id")
                                               @Min(value = 1, message = "ID пользователя должен быть больше нуля") long id) {
        ServiceUser user = userService.findById(id);
        return serviceUserMapper.mapToServiceUserResponse(user);
    }

    /** Метод обработки запроса на изменение данных пользователя. Требуется роль администратора.
     * @param id - переменная, указываемая в url - уникальный идентификатор пользователя.
     * @param request  - объект передачи данных, содержащий сведения о пользователе с
     *                 измененными значениями в виде объекта {@link UpdateUserRequest}.
     * @return - возвращает сервисное сообщение об успешном изменении сведений о пользователе
     * в виде объекта {@link ServiceMessage}
     * */
    @Operation(summary = "Изменение сведений о пользователе")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public ServiceMessage updateUser(@PathVariable("id") long id,
                                     @RequestBody @Valid UpdateUserRequest request) {
       securityService.updateUser(request, id);
       String message = String.format("Сведения о пользователе %s успешно обновлены", request.getUsername());
       log.info(message);
       return new ServiceMessage(HttpStatus.OK.value(), message);
    }

    /** Метод обработки запросов на изменение сведение о пользователе самим пользователем.
     * @param request  - объект передачи данных, содержащий сведения о пользователе с
     *                 измененными значениями в виде объекта {@link UpdateUserRequest}.
     * @return - возвращает сервисное сообщение об успешном изменении сведений о пользователе
     * в виде объекта {@link ServiceMessage}
     */
    @Operation(summary = "Изменение сведений о пользователе самим пользователем")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/my")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public ServiceMessage updateAccount(@RequestBody UpdateUserRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        securityService.updateAccountByUser(request, userDetails.getUsername());
        String message = String.format("Сведения о пользователе %s успешно обновлены", request.getUsername());
        log.info(message);
        return new ServiceMessage(HttpStatus.OK.value(), message);
    }

    /** Метод обработки запросов на удаление записей о пользователе. Требуются права администратора.
     * @param id - идентификатор пользователя
     * @return - возвращает сервисное сообщение об успешном удалении записи о пользователе
     * в виде объекта {@link ServiceMessage}
     */
    @Operation(summary = "Удаление записи о пользователе по уникальному идентификатору")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ServiceMessage deleteUser(@PathVariable("id")
                                         @Min(value = 1, message = "ID пользователя должен быть больше нуля") long id) {
        userService.deleteUserById(id);
        String message = String.format("Сведения о пользователе c id = %s успешно удалены", id);
        log.info(message);
        return new ServiceMessage(HttpStatus.NO_CONTENT.value(), message);
    }

}
