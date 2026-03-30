package com.emobile.springtodo.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/** Класс, описывающий фильтр для выполнения авторизации с использованием JWT-токена */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    /** Утилитарный класс, содержащий методы работы с access-токенами. */
    private final JwtUtils jwtUtils;
    /** Сервис для получения прав доступа пользователя */
    private final UserDetailsService userDetailsService;

    /** Метод проверки access-токена и авторизации пользователя.
     * @param request - запрос, требующий авторизации.
     * @param response - ответ сервера на запрос.
     * @param filterChain  - цепочка фильтров в которой применяется JwtTokenFilter*/
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            final String jwtToken = getToken(request);
            if (jwtToken != null && jwtUtils.validate(jwtToken)) {
                final String username = jwtUtils.getUsername(jwtToken);
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                final UsernamePasswordAuthenticationToken authenticationToken
                        = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception ex) {
            log.error("Ошибка выполнения аутентификации пользователя: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    /** Метод извлечения access-токена из заголовка запроса.
     * @param request - запрос, содержащий заголовок авторизации.
     * @return возвращает строковое представление токена или null, если токен отсутствует.*/
    private String getToken(HttpServletRequest request) {
        final String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwtTokenPrefix = "Bearer ";
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(jwtTokenPrefix)) {
            return headerAuth.substring(jwtTokenPrefix.length());
        }
        return null;
    }
}
