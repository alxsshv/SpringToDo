package com.emobile.springtodo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/** Запрос на обновление истёкшего jwt токена
 * @author Shvariov Alexei
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {

    /** Refresh токен. */
    @NotBlank(message = "refresh-token не может быть пустым")
    @Schema(description = "Refresh token выданный при входе в систему",
            example = "5c9232d5-ce1c-4e03-b620-6251aebe6cdb")
    private String refreshToken;
}
