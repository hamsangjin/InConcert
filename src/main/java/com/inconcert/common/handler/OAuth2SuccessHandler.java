package com.inconcert.common.handler;

import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.role.repository.RoleRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.common.auth.CustomNaverUser;
import com.inconcert.common.auth.jwt.token.entity.Token;
import com.inconcert.common.auth.jwt.token.service.TokenService;
import com.inconcert.common.auth.jwt.util.JwtTokenizer;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.RoleNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final TokenService tokenService;
    private final JwtTokenizer jwtTokenizer;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    // 생성자를 명시적으로 선언해야 핸들러 동작
    public OAuth2SuccessHandler(TokenService tokenService, JwtTokenizer jwtTokenizer, RoleRepository roleRepository, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.jwtTokenizer = jwtTokenizer;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        try {
            CustomNaverUser naverUser = (CustomNaverUser) authentication.getPrincipal();
            String username = naverUser.getName();
            Map<String, Object> attributes = naverUser.getAttributes();

            // 기본 역할 가져오기 (ROLE_USER)
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RoleNotFoundException(ExceptionMessage.ROLE_NOT_FOUND.getMessage()));

            // User 객체 생성
            User tokenUser = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        User user = User.builder()
                                .username(username)
                                .email((String) attributes.get("email"))
                                .roles(new HashSet<>(Collections.singletonList(userRole)))
                                .build();

                        return userRepository.save(user);
                    });

            // SecurityContextHolder에 인증 정보 설정
            Authentication newAuth = new UsernamePasswordAuthenticationToken(naverUser, null, naverUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            // accessToken 생성
            String accessToken = jwtTokenizer.createAccessToken(tokenUser.getId(), tokenUser.getEmail(), username, new ArrayList<>());

            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"accessToken\":\"" + accessToken + "\"}");
            response.setStatus(HttpServletResponse.SC_OK);

            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT / 1000));
            response.addCookie(accessTokenCookie);

            Token tokenEntity = Token.builder()
                    .accessTokenValue(accessToken)
                    .refreshTokenValue("refreshToken")
                    .user(tokenUser)
                    .build();
            tokenService.saveToken(tokenEntity);

            clearAuthenticationAttributes(request);

            response.sendRedirect("/home");
        }
        catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"OAuth2 로그인 처리 중 오류가 발생했습니다.\"}");
        }
    }
}
