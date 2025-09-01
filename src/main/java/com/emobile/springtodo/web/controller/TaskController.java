package com.emobile.springtodo.web.controller;

import com.emobile.springtodo.configuration.AppDefaults;
import com.emobile.springtodo.dto.TaskDto;
import com.emobile.springtodo.dto.mapper.TaskMapper;
import com.emobile.springtodo.dto.response.ServiceMessage;
import com.emobile.springtodo.entity.Board;
import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.entity.Status;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.exception.UserOperationException;
import com.emobile.springtodo.security.AppUserDetails;
import com.emobile.springtodo.service.BoardService;
import com.emobile.springtodo.service.TaskService;
import com.emobile.springtodo.service.UserService;
import com.emobile.springtodo.validation.IsTaskSortProperty;
import com.emobile.springtodo.validation.IsValidDirection;
import io.micrometer.core.instrument.MeterRegistry;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для обработки запросов на получение информации, создание, удаление и изменение задач {@link Task}.
 * @author Aleksey Shvariov
 */
@Tag(name = "Контроллер управления задачами пользователя",
        description = "Контроллер для получение информации о задачах пользователя, их создания, изменения и удаления")
@Validated
@RestController
@RequestMapping("api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    /** Сервис, реализующий логику управления задачами */
    private final TaskService taskService;

    /** Сервис, реализующий логику управления пользователями */
    private final UserService userService;

    /** Сервис, реализующий логику управления досками для задач*/
    private final BoardService boardService;

    /** Маппер для преобразования сущности {@link Task} в объект передачи данных и наоборот */
    private final TaskMapper taskMapper;

    /** Регистер метрик приложения, для внедрения кастомной метрики*/
    private final MeterRegistry meterRegistry;

    /** Метод обработки запросов на создание задач. Требует авторизации пользователя
     * @param taskDto - объект передачи данных, содержащий информацию о создаваемой задачи.
     * @return возвращает сообщение об успешном создании задачи в виде объекта {@link ServiceMessage}
     * @throws UserOperationException будет выброшено, если пользователь пытается добавить задачу на доску для задач,
     * которая ему не принадлежит*/
    @Operation(summary = "Создание задачи")
    @SecurityRequirement(name = "JWT")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceMessage createTask(@RequestBody @Valid TaskDto taskDto,
                                     @AuthenticationPrincipal AppUserDetails userDetails) {
        final Task task = taskMapper.map(taskDto);
        checkBoardOwner(task.getBoardId(), userDetails.getId());
        taskService.createTask(task);
        final String message = String.format("Создана задача %s", task.getTitle());
        log.info(message);
        return new ServiceMessage(HttpStatus.CREATED.value(), message);
    }

    /** Метод проверки является ли пользователь владельцем доски для задач.
     * @param boardId - уникальный идентификатор доски для пользователей.
     * @param userId  - уникальный идентификатор пользователя.
     * @throws UserOperationException будет выброшено, если пользователь не является владельцем доски для задач */
    private void checkBoardOwner(long boardId, long userId) {
        final Board board = boardService.findBoardById(boardId);
        if (!board.getUserId().equals(userId)) {
            throw new UserOperationException("Доска для задач с id = " + boardId +
                    "  не принадлежит данному пользователю. Доступ к задачам других пользователей ограничен");
        }
    }

    /** Метод обработки запросов на получение всех задач определённой доски для задач. Требует авторизации пользователя.
     * @param boardId  - уникальный идентификатор доски для задач;
     * @param size - целочисленное значение количества записей на странице (по умолчанию - 10).
     * @param pageNumber - номер станицы, которую необходимо загрузить (по умолчанию - 0).
     * @param dir - направление сортировки ASC (по возрастанию) или DESC (по убыванию).
     * @param sortProperty - свойство пользователя по которому необходимо выполнять сортировку (по умолчанию id);
     * @return возвращает страницу с объектами передачи данных {@link TaskDto}.
     * @throws UserOperationException будет выброшено, если пользователь пытается получить задачи с доски,
     * которая ему не принадлежит
     */
    @Operation(summary = "Получение постраничного списка задач, размещенных на выбранной доске")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedModel<TaskDto> getAllTasksByBoard(@RequestParam(value = "board")
                                                  @Min(value = 1, message = "Некорректный идентификатор доски") long boardId,
                                                  @RequestParam(value = "size", defaultValue = AppDefaults.PAGE_SIZE)
                                                  @Min(value = 1, message = "Размер станицы не может быть меньше единицы")
                                                  @Max(value = 100, message = "Размер страницы не должен превышать 100 записей") int size,
                                                  @RequestParam(value = "page", defaultValue = AppDefaults.PAGE_NUMBER)
                                                  @Min(value = 0, message = "Некорректное значение номера страницы") int pageNumber,
                                                  @RequestParam(value = "dir", defaultValue = AppDefaults.PAGE_DIRECTION)
                                                  @IsValidDirection String dir,
                                                  @RequestParam(value = "sortBy", defaultValue = AppDefaults.PAGE_SORT_PROP)
                                                  @IsTaskSortProperty String sortProperty,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        final ServiceUser user = userService.findByUsernameOrEmail(userDetails.getUsername());
        checkBoardOwner(boardId, user.getId());
        final Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.valueOf(dir), sortProperty));
        final Page<TaskDto> taskDtoPage = taskService.findAllTaskByBoardId(boardId, pageable).map(taskMapper::map);
        return new PagedModel<>(taskDtoPage);
    }

    /** Метод обработки запросов на получение задачи по id. Требует авторизации пользователя.
     * @param id - уникальный идентификатор задачи
     * @return возвращает сведения о задаче в виде объекта передачи данных {@link TaskDto}
     * @throws UserOperationException будет выброшено, если пользователь не является владельцем доски для задач,
     * на которой размещена указанная задача.
     */
    @Operation(summary = "Получение задачи по уникальному идентификатору")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDto getTaskById(@PathVariable("id")
                                   @Min(value = 1, message = "Некорректный идентификатор задачи") long id,
                               @AuthenticationPrincipal AppUserDetails userDetails) {
        final Task task = taskService.findTaskById(id);
        checkBoardOwner(task.getBoardId(), userDetails.getId());
        return taskMapper.map(task);
    }

    /** Метод обработки запросов на изменение сведений о задаче. Требуется авторизация пользователя.
     * @param taskDto - объект передачи данных, содержащий сведения о задаче с учетом требуемых изменений.
     * @return возвращает сообщение об успешном изменении задачи в виде объекта {@link ServiceMessage}
     * @throws UserOperationException будет выброшено, если пользователь пытается обновить задачу,
     * размещённую на чужой доске для задач.
     */
    @Operation(summary = "Изменение задачи")
    @SecurityRequirement(name = "JWT")
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ServiceMessage updateTask(@RequestBody @Valid TaskDto taskDto,
                                     @AuthenticationPrincipal AppUserDetails userDetails) {
        final Task task = taskMapper.map(taskDto);
        checkBoardOwner(task.getBoardId(), userDetails.getId());
        taskService.updateTask(task);
        if (task.getStatus().equals(Status.COMPLETED)) {
            meterRegistry.counter("completed_task_counter", List.of()).increment();
        }
        final String message = String.format("Задача %s успешно обновлена", task.getTitle());
        log.info(message);
        return new ServiceMessage(HttpStatus.OK.value(), message);
    }

    /** Метод обработки запросов на удаление задачи. Требует авторизации пользователя.
     * @param id - уникальный идентификатор удаляемой задачи.
     * @return возвращает сообщение об успешном удалении задачи в виде объекта {@link ServiceMessage}
     * @throws UserOperationException будет выброшено, если пользователь пытается удалить задачу,
     * размещённую на чужой доске для задач
     * */
    @Operation(summary = "Удаление задачи")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ServiceMessage deleteTaskById(@PathVariable("id")
                                         @Min(value = 1, message = "Некорректный идентификатор задачи") long id,
                                         @AuthenticationPrincipal AppUserDetails userDetails) {
        final Task task = taskService.findTaskById(id);
        checkBoardOwner(task.getBoardId(), userDetails.getId());
        taskService.deleteById(id);
        final String message = String.format("Задача c id = %s успешно удалена", id);
        log.info(message);
        return new ServiceMessage(HttpStatus.NO_CONTENT.value(), message);
    }

}
