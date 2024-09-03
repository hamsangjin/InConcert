package com.inconcert.common.handler;

import com.inconcert.domain.user.entity.Role;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.common.auth.CustomNaverUser;
import com.inconcert.common.auth.jwt.token.entity.Token;
import com.inconcert.common.auth.jwt.token.service.TokenService;
import com.inconcert.common.auth.jwt.util.JwtTokenizer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final TokenService tokenService;
    private final JwtTokenizer jwtTokenizer;
    private final UserRepository userRepository;

    // 생성자를 명시적으로 선언해야 핸들러 동작
    public OAuth2SuccessHandler(TokenService tokenService, JwtTokenizer jwtTokenizer, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.jwtTokenizer = jwtTokenizer;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        try {
            CustomNaverUser naverUser = (CustomNaverUser) authentication.getPrincipal();
            String username = naverUser.getName();
            Map<String, Object> attributes = naverUser.getAttributes();

            // User 객체 생성
            User tokenUser = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        User user = User.builder()
                                .username(username)
                                .email((String) attributes.get("email"))
                                .role(Role.ROLE_USER)
                                .build();

                        return userRepository.save(user);
                    });

//             이용 정지 당한 경우
            if(tokenUser.getBanDate().isAfter(LocalDate.now())){
                response.setStatus(HttpStatus.LOCKED.value());
                response.getWriter().write("{\"error\": \"Account is locked due to a ban.\"}");
                response.sendRedirect("/loginform");
                return; // 더 이상 진행하지 않도록 종료
            }
//            // 이용 정지 당한 경우
//            if(tokenUser.getBanDate().isAfter(LocalDate.now())){
//                response.setStatus(HttpStatus.LOCKED.value());
//                response.setContentType("application/json;charset=UTF-8");
//                response.getWriter().write("{\"error\": \"Account is locked due to a ban.\", \"banDate\": \"" + tokenUser.getBanDate() + "\"} ");
//                return; // 더 이상 진행하지 않도록 종료
//            }

            // SecurityContextHolder에 인증 정보 설정
            Authentication newAuth = new UsernamePasswordAuthenticationToken(naverUser, null, naverUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            // accessToken 생성
            String accessToken = jwtTokenizer.createAccessToken(tokenUser.getId(), tokenUser.getEmail(), username, tokenUser.getRole());

            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"accessToken\":\"" + accessToken + "\"}");
            response.setStatus(HttpServletResponse.SC_OK);

            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT / 1000));
            response.addCookie(accessTokenCookie);

            Optional<Token> optionalToken = tokenService.getTokenByUser(tokenUser);

            // 로그인한 적 있는 경우 토큰 업데이트
            if(optionalToken.isPresent()) {
                Token token = optionalToken.get();
                token.updateToken(accessToken, "refreshToken");
                tokenService.saveToken(token);
            }
            else {
                Token tokenEntity = Token.builder()
                        .accessTokenValue(accessToken)
                        .refreshTokenValue("refreshToken")
                        .user(tokenUser)
                        .build();
                tokenService.saveToken(tokenEntity);
            }

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