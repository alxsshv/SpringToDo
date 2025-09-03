package com.emobile.springtodo.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


/** Конфигурация для настройки планировщика задач
 *  (выполнение задач (методов) с определённой периодичностью). */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "app.scheduler", name = "enable", matchIfMissing = true)
public class SchedulerConfiguration {

}
