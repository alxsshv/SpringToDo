package com.emobile.springtodo.web.controller;

import com.emobile.springtodo.dto.BoardDto;
import com.emobile.springtodo.dto.mapper.BoardMapper;
import com.emobile.springtodo.dto.response.ServiceMessage;
import com.emobile.springtodo.entity.Board;
import com.emobile.springtodo.exception.UserOperationException;
import com.emobile.springtodo.security.AppUserDetails;
import com.emobile.springtodo.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Класс, описывающий контроллер для обработки запросов на работу с досками для задач {@link Board}.
 * @author Aleksey Shvariov
 */
@Tag(name = "Контроллер управления досками для задач", description = "Контроллер обрабатывает запросы на создание," +
        " удаление и изменение досок для задач")
@RestController
@RequestMapping("api/v1/boards")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    /** Маппер для преобразования {@link Board} в {@link BoardDto} и {@link BoardDto} в {@link Board}*/
    private final BoardMapper boardMapper;

    /** Сервис, реализующий логику работы с досками для задач {@link Board}*/
    private final BoardService boardService;

    /** Создание доски для задач
     * @param boardDto - объект передачи данных для доски для задач {@link BoardDto}
     * @return возвращает сервисное сообщение об успешном выполнении запроса {@link ServiceMessage}*/
    @Operation(summary = "Создание доски для задач",
            description = "Добавляет новую доску для задач пользователя")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceMessage createBoard(@Parameter(required = true) @RequestBody @Valid BoardDto boardDto,
                                      @AuthenticationPrincipal AppUserDetails userDetails) {
        Board board = boardMapper.map(boardDto);
        boardService.createBoard(board, userDetails.getId());
        String message = String.format("Добавлена доска для задач %s", board.getTitle());
        log.info(message);
        return new ServiceMessage(HttpStatus.CREATED.value(), message);
    }


    /** Получение списка всех досок для задач аутентифицированного пользователя.
     * @return возвращает список (List) объектов передачи данных доски для задач {@link BoardDto} */
    @Operation(summary = "Получение всех досок для задач, созданных пользователем")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BoardDto> getAllByUser(@AuthenticationPrincipal AppUserDetails userDetails) {
        List<Board> boards = boardService.findAllBoardsByUserId(userDetails.getId());
        return boardMapper.map(boards);
    }

    /** Получение доски для задач по уникальному идентификатору
     * @param id - уникальный идентификатор задачи в формате Long,
     * @return возвращает объект передачи данных доски для задач {@link BoardDto}
     * @throws UserOperationException будет выброшено, если пользователь не является владельцем запрашиваемой доски */
    @Operation(summary = "Получение задачи по уникальному идентификатору",
            description = "Возвращает задачу только если она запрошена владельцем")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BoardDto getBoardById(@PathVariable("id") long id,
                                 @AuthenticationPrincipal AppUserDetails userDetails) {
        Board board = boardService.findBoardById(id);
        if (!board.getUserId().equals(userDetails.getId())) {
            throw new UserOperationException("Недостаточно прав для просмотра сведений о данной доске");
        }
        return boardMapper.map(board);
    }

    /** Обновление информации о доске до задач. В текущей версии допускается обновление только поля title.
     * @param boardDto - объект передачи данных для доски для задач {@link BoardDto}
     * @return сервисное сообщение об успешном обновлении {@link ServiceMessage}
     * @throws UserOperationException выбрасывается в случае,
     * если пользователь не является владельцем изменяемой доски для задач*/
    @Operation(summary = "Обновление информации о доске для задач",
            description = "Обновляет название доски для задач")
    @SecurityRequirement(name = "JWT")
    @PutMapping()
    @ResponseStatus(HttpStatus.OK)
    public ServiceMessage updateBoard(@RequestBody @Valid BoardDto boardDto,
                                      @AuthenticationPrincipal AppUserDetails userDetails) {
        if (!boardDto.getUserId().equals(userDetails.getId())) {
            throw new UserOperationException("Недостаточно прав для изменения сведений о доске");
        }
        boardService.updateBoardTitle(boardMapper.map(boardDto));
        final String message = String.format("Название доски для задач изменено на %s", boardDto.getTitle());
        log.info(message);
        return new ServiceMessage(HttpStatus.OK.value(), message);
    }

    /** Удаление доски для задач по уникальному идентификатору.
     * @param id - уникальный идентификатор доски для задач, которую необходимо удалить.
     * @return возвращает сервисное сообщение об успешном удалении доски для задач {@link ServiceMessage}
     * @throws UserOperationException будет выброшено, если пользователь не является владельцем удаляемой доски
     * */
    @Operation(summary = "Удаление доски для задач по уникальному идентификатору")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ServiceMessage deleteById(@PathVariable("id") long id,
                                     @AuthenticationPrincipal AppUserDetails userDetails) {
        final Board board = boardService.findBoardById(id);
        if (!board.getUserId().equals(userDetails.getId())) {
            throw new UserOperationException("Недостаточно прав для удаления доски");
        }
        boardService.deleteBoardById(id);
        final String message = String.format("Доска для задач c id = %s успешно удалена", id);
        log.info(message);
        return new ServiceMessage(HttpStatus.NO_CONTENT.value(), message);
    }

}
