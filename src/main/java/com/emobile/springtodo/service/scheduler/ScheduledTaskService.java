package com.emobile.springtodo.service.scheduler;

import com.emobile.springtodo.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Сервис для выполнения запланированных задач с определённой периодичностью */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    /** Сервис для управления refresh-токенами*/
    private final RefreshTokenService refreshTokenService;

    /** Метод периодического удаления просроченных токенов, выполняемый через установленный временной интервал. */
    @Scheduled(fixedDelayString = "${app.scheduler.expirationTokenDeleteInterval}")
    public void deleteExpiredRefreshTokens() {
        refreshTokenService.deleteExpiredToken();
        log.info("Планировщик задач: Выполнено удаление просроченных токенов");
    }
}
