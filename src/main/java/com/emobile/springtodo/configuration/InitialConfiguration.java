package com.emobile.springtodo.configuration;

import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.security.SecurityRole;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

/** Конфигурация для создания администратора приложения по умолчанию. */
@Configuration
@ConfigurationProperties(prefix = "app.security.administrator")
@RequiredArgsConstructor
@Setter
@Validated
@Slf4j
public class InitialConfiguration {

    /** Репозиторий для хранения пользователей */
    @Autowired
    private final UserRepository userRepository;

    /** Кодировщик паролей пользователей */
    @Autowired
    private final PasswordEncoder passwordEncoder;

    /** Имя пользователя (логин) администратора*/
    private String username;

    /** Адрес электронной почты администратора */
    private String email;

    /** Пароль администратора */
    private String password;

    /** Флаг, определяющий требуется ли создавать администратора системы. */
    private boolean enabled;

    /** Метод создания администратора системы по умолчанию Данные администратора
     * должны быть представлены в файле application.yml.
     * Выполняется один раз после запуска приложения */
    @PostConstruct
    public void initializeDefaultAdministrator() {
        if (username == null || email == null || password == null) {
            log.warn("ВНИМАНИЕ! Свойства приложения не содержат сведения," +
                    "необходимые для регистрации администратора." +
                    "Аккаунт администратора не доступен для входа в систему");
        } else if (enabled && userRepository.findByUsernameOrEmail(username, email).isEmpty()) {
            ServiceUser user = ServiceUser.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .roles(Set.of(SecurityRole.ROLE_ADMIN))
                    .build();
            userRepository.save(user);
            log.info("Зарегистрирован администратор системы");
        }
    }

}
