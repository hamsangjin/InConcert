package com.inconcert.common.auth.jwt.util;

import com.inconcert.domain.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenizer {
    private final byte[] accessSecret;
    private final byte[] refreshSecret;

    public static Long ACCESS_TOKEN_EXPIRE_COUNT;
    public static Long REFRESH_TOKEN_EXPIRE_COUNT;

    public JwtTokenizer(@Value("${jwt.secretKey}") String accessSecret,
                        @Value("${jwt.refreshKey}") String refreshSecret,
                        @Value("${jwt.access-token-expire-time}") Long accessTokenExpireCount,
                        @Value("${jwt.refresh-token-expire-time}") Long refreshTokenExpireCount) {
        this.accessSecret = accessSecret.getBytes(StandardCharsets.UTF_8);
        this.refreshSecret = refreshSecret.getBytes(StandardCharsets.UTF_8);
        JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT = accessTokenExpireCount;
        JwtTokenizer.REFRESH_TOKEN_EXPIRE_COUNT = refreshTokenExpireCount;
    }

    // JWT 생성
    private String createToken(Long id, String email, String username,
                               Role role, Long expire, byte[] secretKey) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", id);
        claims.put("username", username);
        claims.put("role", role.name()); // Role enum을 문자열로 저장

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + expire))
                .signWith(getSigningKey(secretKey))
                .compact();
    }

    // 서명 키 생성 메소드
    public static Key getSigningKey(byte[] secretKey) {
        return Keys.hmacShaKeyFor(secretKey);
    }

    // ACCESS Token 생성
    public String createAccessToken(Long id, String email, String username, Role roles) {
        return createToken(id, email, username, roles, ACCESS_TOKEN_EXPIRE_COUNT, accessSecret);
    }

    // Refresh Token 생성
    public String createRefreshToken(Long id, String email, String username, Role roles) {
        return createToken(id, email, username, roles, REFRESH_TOKEN_EXPIRE_COUNT, refreshSecret);
    }

    // 토큰 파싱 및 검증
    public Claims parseToken(String token, byte[] secretKey){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims parseAccessToken(String accessToken) {
        return parseToken(accessToken, accessSecret);
    }

    public Claims parseRefreshToken(String refreshToken) {
        return parseToken(refreshToken, refreshSecret);
    }

    // Role enum으로 역직렬화
    public Role getRoleFromClaims(Claims claims) {
        String roleName = claims.get("role", String.class); // 문자열로 저장된 Role 가져오기
        return Role.valueOf(roleName); // 문자열을 Role enum으로 변환
    }
}