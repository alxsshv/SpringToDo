package com.emobile.springtodo.dto.request;

import com.emobile.springtodo.validation.EmailNotExist;
import com.emobile.springtodo.validation.UsernameNotExist;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

/** Класс описывает содержание запроса на создание пользователя.
 *  @author Shvariov Alexei
 * */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Запрос на создание пользователя")
public class CreateUserRequest {

    /**Имя пользователя.*/
    @UsernameNotExist
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Schema(description = "Имя пользователя (логин)", example = "ivanov")
    private String username;

    /** Адрес электронной почты. */
    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Неверный формат адреса электронной почты")
    @EmailNotExist
    @Schema(description = "Адрес электронной почты пользователя", example = "ivanov@email.com")
    private String email;

    /** Пароль пользователя. */
    @Size(min = 4, max = 50, message = "Пароль должен содержать не менее 4 и не более 50 символов")
    @NotBlank(message = "Пароль не может быть пустым")
    @Schema(description = "Пароль пользователя для входа в систему",
            example = "abc1234", minLength = 3, maxLength = 50)
    private String password;

    /** Роли пользователя в строковом формате. */
    @NotEmpty(message = "Необходимо указать одну или несколько ролей пользователя. Доступные роли ROLE_ADMIN, ROLE_USER")
    @Schema(description = "список ролей пользователя", example = "[ROLE_USER]",
            allowableValues = {"ROLE_USER", "ROLE_ADMIN"})
    private Set<String> roleNames;
}
