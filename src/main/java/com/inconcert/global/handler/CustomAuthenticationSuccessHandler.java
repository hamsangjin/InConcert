package com.inconcert.global.handler;

import com.inconcert.global.auth.CustomUserDetails;
import com.inconcert.global.auth.jwt.util.JwtTokenizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenizer jwtTokenizer;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        log.info("Form login success for user: {}", userDetails.getUsername());

        String accessToken = jwtTokenizer.createAccessToken(
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .collect(Collectors.toList())
        );

        // 쿠키 설정
        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        log.info("AccessToken cookie set: {}", cookie.getValue());

        // 응답 헤더에 Set-Cookie가 포함되어 있는지 확인하는 로그 추가
        log.info("Response Headers: {}", response.getHeaderNames().stream()
                .collect(Collectors.toMap(name -> name, name -> response.getHeaders(name))));

        log.info("Redirecting to /home");
        response.sendRedirect("/home");
    }
}
