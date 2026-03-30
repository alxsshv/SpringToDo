package com.emobile.springtodo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

/** Класс описывает содержание запроса на изменение сведений о пользователе.
 *  @author Shvariov Alexei
 * */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Запрос на изменение сведений о пользователе")
public class UpdateUserRequest {

    /**Имя пользователя.*/
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Schema(description = "Имя пользователя (логин). Данное поле не может быть изменено", example = "ivanov")
    private String username;

    /** Адрес электронной почты. */
    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Неверный формат адреса электронной почты")
    @Schema(description = "Адрес электронной почты пользователя", example = "ivanoff@email.com")
    private String email;

    /** Пароль пользователя. */
    @Size(min = 4, max = 50, message = "Пароль должен содержать не менее 4 и не более 50 символов")
    @Schema(description = "Пароль пользователя для входа в систему",
            example = "abc1234", minLength = 4, maxLength = 50)
    private String password;

    /** Роли пользователя в строковом формате. */
    @NotEmpty(message = "Необходимо указать одну или несколько ролей пользователя. Доступные роли ROLE_ADMIN, ROLE_USER")
    @Schema(description = "список ролей пользователя",
            example = "[ROLE_USER]", allowableValues = {"ROLE_USER", "ROLE_ADMIN"})
    private Set<String> roleNames;
}
