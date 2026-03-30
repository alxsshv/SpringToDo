package com.emobile.springtodo.dto.request;

import com.emobile.springtodo.validation.EmailNotExist;
import com.emobile.springtodo.validation.UsernameNotExist;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Класс описывает содержание запроса на регистрацию пользователя.
 * @author Shvariov Alexei
 * */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Запрос на регистрацию пользователя")
public class RegisterUserRequest {

    /** Имя пользователя (логин) */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать не менее 3 и не более 50 символов")
    @UsernameNotExist
    @Schema(description = "Имя пользователя для входа в систему", example = "ivanov", minLength = 3, maxLength = 50)
    private String username;

    /** Адрес электронной почты пользователя */
    @NotBlank(message = "Пожалуйста укажите адрес электронной почты")
    @Email(message = "Неверный формат адреса электронной почты. " +
            "Пожалуйста проверьте правильность указанного email")
    @EmailNotExist
    @Schema(description = "Адрес электронной почты пользователя",
            example = "Ivanov@email.com")
    private String email;

    /** Пароль пользователя для входа в сервис */
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 4, max = 50, message = "Пароль должен содержать не менее 4 и не более 50 символов")
    @Schema(description = "Пароль пользователя для входа в систему",
            example = "abc1234", minLength = 3, maxLength = 50)
    private String password;
}
