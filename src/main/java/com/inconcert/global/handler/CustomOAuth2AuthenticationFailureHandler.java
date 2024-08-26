package com.inconcert.global.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 authentication failed", exception);

        // OAuth2 인증 실패 시 예외 메시지 확인
        String errorMessage = exception.getMessage();

        // 사용자가 인증을 취소한 경우 home으로 리디이렉션
        if (errorMessage != null && errorMessage.contains("access_denied")) {
            redirectStrategy.sendRedirect(request, response, "/home");
        }
        // 그 외의 오류에 대해서는 에러 메시지 반환
        else {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"error\": \"인증에 실패했습니다. 다시 시도해 주세요.\"}");
        }
    }
}
