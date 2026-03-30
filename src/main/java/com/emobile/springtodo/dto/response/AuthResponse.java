package com.emobile.springtodo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Ответ сервера при успешной аутентификации и авторизации пользователя.
 * @author Shvariov Alexei
 * */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Ответ сервера при успешной аутентификации и авторизации пользователя")
public class AuthResponse {

    /** Идентификатор пользователя */
    @Schema(description = "Идентификатор пользователя", example = "1")
    private long id;

    /** Имя пользователя */
    @Schema(description = "Имя пользователя для входа в систему",
            example = "Ivanov", minLength = 3, maxLength = 50)
    private String username;

    /** Адрес электронной почты */
    @Schema(description = "Адрес электронной почты пользователя",
            example = "Ivanov@email.com")
    private String email;

    /** Роли пользователя */
    @Schema(description = "Набор ролей пользователей (определяют его права доступа)",
            example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
    private List<String> roles;

    /** Refresh токен, выданный пользователю при входе в систему */
    @Schema(description = "Refresh токен, выданный пользователю при входе в систему")
    private String refreshToken;

    /** Access токен, выданный пользователю при входе в систему */
    @Schema(description = "Access токен, выданный пользователю при входе в систему")
    private String accessToken;


}
