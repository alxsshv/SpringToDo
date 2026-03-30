package com.emobile.springtodo.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;


/** Утилитарный класс, содержащий методы работы с access-токенами. */
@Component
@Slf4j
public class JwtUtils {

    /** Секретная строка для генерации подписания токена. */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Срок действия токена */
    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    /** Метод формирования access-токена.
     * @param username - имя пользователя, которому принадлежит access-токен.
     * @return возвращает строковое представление токена.*/
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + tokenExpiration.toMillis()))
                .signWith(generateSignatureSecretKey(jwtSecret))
                .compact();
    }

    /** Метод генерации объекта класса {@link SecretKey} на основе секретной строки,
     *  переданной в качестве параметра.
     *  @param jwtSecret - строковое представление секретного ключа.
     *  @return возвращает объект класса {@link SecretKey}
     */
    private SecretKey generateSignatureSecretKey(String jwtSecret) {
        byte[] secretKeyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }

    /** Метод извлечения имени пользователя из токена.
     * @param token - строковое представление токена.
     * @return возвращает имя пользователя, которому принадлежит токен.*/
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    /** Метод извлечения информативных частей токена.
     * @param token - строковое представление токена.
     * @throws MalformedJwtException будет выброшено, если токен неправильно составлен.
     * @throws ExpiredJwtException будет выброшено, если срок действия токена истек.
     * @throws UnsupportedJwtException будет выброшено, если данный тип токена не поддерживается.
     * @throws IllegalArgumentException будет выброшено, если токен не содержит данные или данные некорректны.
     * */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(generateSignatureSecretKey(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**Метод проверки валидности полученного access-токена.
     * @param token - строковое представление access-токена.
     * @return возвращает true, если токен валидный (по нему можно аутентифицировать пользователя)
     * или false, если токен не валидный.*/
    public boolean validate(String token) {
        try {
            getClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Недействительный токен: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Токен просрочен: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Токен не поддерживается: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Токен не содержит данные или данные не корректны: {}", ex.getMessage());
        }
        return false;
    }

}
