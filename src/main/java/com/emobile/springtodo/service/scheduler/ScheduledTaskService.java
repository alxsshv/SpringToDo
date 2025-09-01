package com.emobile.springtodo.service.scheduler;

import com.emobile.springtodo.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final RefreshTokenService refreshTokenService;


    @Scheduled(fixedDelayString = "${app.scheduler.expirationTokenDeleteInterval}")
    public void deleteExpiredRefreshTokens() {
        refreshTokenService.deleteExpiredToken();
        log.info("Планировщик задач: Выполнено удаление просроченных токенов");
    }
}
