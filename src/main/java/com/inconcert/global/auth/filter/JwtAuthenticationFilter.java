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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // accessToken 가져오기
        String token = getToken(request);

        // header 에 값이 있는지 확인
        if(StringUtils.hasText(token)){
            try{
                getAuthentication(token);
            }catch (ExpiredJwtException e){
                log.error("Expired Token : {}",token,e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired token");
                return;
            }catch (UnsupportedJwtException e){
                log.error("Unsupported Token: {}", token, e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unsupported token");
                return;
            } catch (MalformedJwtException e) {
                log.error("Invalid Token: {}", token, e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            } catch (IllegalArgumentException e) {
                log.error("Token not found: {}", token, e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not found");
                return;
            } catch (Exception e) {
                log.error("JWT Filter - Internal Error: {}", token, e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT filter internal error");
                return;
            }
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

    private List<GrantedAuthority> getGrantedAuthorities(Claims claims){
        List<String> roles = (List<String>) claims.get("roles");

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles){
            authorities.add(()->role);  // role 찾아서 권한에 추가
        }
        return authorities;
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