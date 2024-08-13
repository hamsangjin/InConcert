package com.inconcert.global.auth.filter;

import com.inconcert.global.auth.CustomUserDetails;
import com.inconcert.global.auth.jwt.token.JwtAuthenticationToken;
import com.inconcert.global.auth.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // accessToken 가져오기
            String token = getToken(request);
            if (StringUtils.hasText(token)) {
                getAuthentication(token);
            }
        } catch (ExpiredJwtException e) {
            log.error("Expired Token : {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"토큰이 만료되었습니다.\"}");
            return;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported Token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"유효하지 않은 토큰입니다.\"}");
            return;
        } catch (MalformedJwtException e) {
            log.error("Invalid Token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"유효하지 않은 토큰입니다.\"}");
            return;
        } catch (IllegalArgumentException e) {
            log.error("Token not found: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"토큰이 없습니다.\"}");
            return;
        } catch (Exception e) {
            log.error("JWT Filter - Internal Error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"내부 서버 오류가 발생했습니다.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void getAuthentication(String token){
        Claims claims = jwtTokenizer.parseAccessToken(token);   // 토큰 파싱

        String email = claims.getSubject(); // 이메일 기준으로 찾기

        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        List<GrantedAuthority> authorities = getGrantedAuthorities(claims);

        CustomUserDetails userDetails = new CustomUserDetails(userId, username, "", email, authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        Authentication authentication = new JwtAuthenticationToken(authorities, userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);   // contextholder에 인증 설정
    }

    private List<GrantedAuthority> getGrantedAuthorities(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> role)
                .collect(Collectors.toList());
    }

    private String getToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        // Bearer 토큰인지 확인
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        // request의 쿠키 찾아서 accessToken 확인
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}