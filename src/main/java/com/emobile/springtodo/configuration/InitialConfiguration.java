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

@Configuration
@ConfigurationProperties(prefix = "app.security.administrator")
@RequiredArgsConstructor
@Setter
@Validated
@Slf4j
public class InitialConfiguration {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    private String username;
    private String email;
    private String password;
    private boolean enabled;

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
