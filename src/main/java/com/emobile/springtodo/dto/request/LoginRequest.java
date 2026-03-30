package com.emobile.springtodo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/** Класс описывает содержание запроса на вход в систему в качестве авторизованного пользователя
 * @author Shvariov Alexei
 * */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Schema(description = "Запрос на вход в систему в качестве авторизованного пользователя")
public class LoginRequest {

    /**Имя пользователя или адрес электронной почты*/
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Schema(description = "Имя пользователя или адрес электронной почты")
    private String usernameOrEmail;


    /**Пароль пользователя для входа в сервис*/
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 4, max = 50, message = "Пароль должен содержать не менее 4 и не более 50 символов")
    private String password;
}
