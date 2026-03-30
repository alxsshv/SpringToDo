package com.emobile.springtodo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Ответ сервера на запрос обновления jwt токена
 * @author Shvariov Alexei
 * */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Ответ сервера на запрос обновления jwt токена")
public class RefreshTokenResponse {

    /** Refresh token */
    @Schema(description = "Access token выданный при входе в систему")
    private String refreshToken;

    /** Access token */
    @Schema(description = "Access token выданный при входе в систему")
    private String accessToken;
}
