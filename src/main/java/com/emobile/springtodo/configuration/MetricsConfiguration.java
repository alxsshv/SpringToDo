package com.emobile.springtodo.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfiguration {

    @Bean
    MeterBinder meterBinder() {
        return meterRegistry -> {
            Counter.builder("completed_task_counter")
                    .description("Количество выполненных задач")
                    .register(meterRegistry);
        };
    }
}
