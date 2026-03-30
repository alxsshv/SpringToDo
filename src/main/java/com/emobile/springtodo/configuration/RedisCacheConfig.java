package com.emobile.springtodo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

/** Конфигурация кэширования данных приложения с использованием Redis */
@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

    /** Адрес распределённого кэша (Redis - инстанса) */
    @Value("${spring.redis.host}")
    private String redisHost;

    /** Порт для подключения к Redis */
    @Value("${spring.redis.port}")
    private int redisPort;

    /** Фабрика подключений к Redis c использованием jedis. */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
    }

    /** Объект управляющий кэшированием данных в Redis */
    @Bean
    public RedisCacheManager cacheManager() {
        return RedisCacheManager.builder(jedisConnectionFactory()).build();
    }

}
