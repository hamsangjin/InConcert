package com.inconcert.domain.user.controller;

import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.user.dto.request.LogInReqDto;
import com.inconcert.domain.user.dto.response.LoginRspDto;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.auth.jwt.token.entity.Token;
import com.inconcert.global.auth.jwt.token.service.TokenService;
import com.inconcert.global.auth.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserApiController {
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtTokenizer jwtTokenizer;
     private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LogInReqDto logInReqDto,
                                BindingResult bindingResult, HttpServletResponse response) {
        // Dto의 유효성 검사에 오류가 있는 경우
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        User user = userService.findByUsername(logInReqDto.getUsername());

        // 조회한 유저의 비밀번호와 입력한 비밀번호 일치하지 않은 경우
        if(!passwordEncoder.matches(logInReqDto.getPassword(), user.getPassword())) {
            return new ResponseEntity("비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        // 토큰 발급
        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), user.getUsername(), roles);
        String refreshToken = jwtTokenizer.createRefreshToken(user.getId(), user.getEmail(), user.getUsername(), roles);

        // accessToken, refreshToken 을 DB에 저장
        Token tokenEntity = Token.builder()
                .accessTokenValue(accessToken)
                .refreshTokenValue(refreshToken)
                .user(user)
                .build();

        tokenService.saveToken(tokenEntity);

        LoginRspDto loginRspDto = LoginRspDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .build();

        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT / 1000)); // 쿠키의 유지시간의 단위는 초, 토큰의 유지시간의 단위는 밀리초

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.REFRESH_TOKEN_EXPIRE_COUNT / 1000));

        // 응답 객체에 쿠키를 추가
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // 로그 추가
        log.info("Set-Cookie: accessToken={}", accessTokenCookie.getValue());
        log.info("Set-Cookie: refreshToken={}", refreshTokenCookie.getValue());
        log.info("Response Headers: {}", response.getHeaderNames().stream()
                .collect(Collectors.toMap(name -> name, name -> response.getHeaders(name))));

        return new ResponseEntity(loginRspDto, HttpStatus.OK);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response) {
        //할일!!
        //1. 쿠키로부터 refresh Token을 얻어온다.
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        //2-1. 없을때.
        //오류로 응답
        if (refreshToken == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        //2-2. 있을때.
        //토큰으로부터 정보를 얻어온다.
        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);

        String username = (String) claims.get("username");
        User user = userService.findByUsername(username);
        // orElseThrow(() -> new IllegalArgumentException("사용자를 찾지 못했습니다."));
        //예외 처리 해라
//        if(user == null) {
//
//        }

        //3. accessToken 생성.
        List roles = (List) claims.get("roles");
        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), username, roles);


        //4. 쿠키 생성 response로 보내고,
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT / 1000)); // 초 단위로 넘어오니까 밀리로 바꾸기 위해 1000으로 나눔.

        response.addCookie(accessTokenCookie);

        LoginRspDto loginRspDto = LoginRspDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .build();

        return new ResponseEntity(loginRspDto, HttpStatus.OK);
    }
}
