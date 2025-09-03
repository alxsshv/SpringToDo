package com.emobile.springtodo.configuration;

import com.emobile.springtodo.security.jwt.JwtAuthenticationEntryPoint;
import com.emobile.springtodo.security.jwt.JwtTokenFilter;
import com.emobile.springtodo.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Конфигурация параметров безопасности */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    /** Сервис для получения информации о привилегиях (ролях) пользователя */
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /** Объект, формирующий ответ пользователю при неудачной аутентификации */
    @Autowired
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    /** Фильтр для аутентификации пользователя при помощи jwt-токена */
    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    /** Кодировщик паролей пользователя. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Провайдер для аутентификации пользователя при помощи данных полученных из БД */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService);
        return authenticationProvider;
    }

    /** Объект управляющий аутентификацией пользователя */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** Цепочка фильтров (объект определяющий параметры безопасности приложения). */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security.authorizeHttpRequests((authRegistry) -> authRegistry
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(configurer -> configurer
                        .authenticationEntryPoint(authenticationEntryPoint))
                .authenticationProvider(daoAuthenticationProvider())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return security.build();

    }



}
