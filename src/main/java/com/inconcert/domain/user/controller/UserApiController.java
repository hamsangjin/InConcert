package com.inconcert.domain.user.controller;

import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.user.dto.request.*;
import com.inconcert.domain.user.dto.response.*;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.auth.CustomUserDetails;
import com.inconcert.global.auth.jwt.token.entity.Token;
import com.inconcert.global.auth.jwt.token.service.TokenService;
import com.inconcert.global.auth.jwt.util.JwtTokenizer;
import com.inconcert.global.exception.UserNotFoundException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserApiController {
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtTokenizer jwtTokenizer;
    private final AuthenticationManager authenticationManager;

    // 로그인
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LogInReqDto reqDto, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(reqDto.getUsername(), reqDto.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername());
            List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

            String accessToken = jwtTokenizer.createAccessToken(
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getUsername(),
                    userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList())
            );

            String refreshToken = jwtTokenizer.createRefreshToken(userDetails.getId(), userDetails.getEmail(), userDetails.getUsername(), roles);

            Token tokenEntity = Token.builder()
                    .accessTokenValue(accessToken)
                    .refreshTokenValue(refreshToken)
                    .user(user)
                    .build();

            tokenService.saveToken(tokenEntity);

            LoginRspDto loginRspDto = LoginRspDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .username(userDetails.getUsername())
                    .build();

            // 쿠키 설정
            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT / 1000));

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.REFRESH_TOKEN_EXPIRE_COUNT / 1000));

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(loginRspDto);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키로부터 refresh Token을 얻어온다.
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

        // 없을 때 오류로 응답
        if (refreshToken == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        // 있을 때 토큰으로부터 정보를 얻어온다.
        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);
        String username = (String) claims.get("username");

        User user = userService.findByUsername(username);

        if(user == null) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }

        // accessToken 생성.
        List roles = (List) claims.get("roles");
        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), username, roles);


        // 쿠키 생성 response로 보내기
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

    // 아이디 중복 확인
    @PostMapping("/user/id-check")
    public ResponseEntity<? super UsernameCheckRspDto> usenameCheck(@RequestBody @Valid UsernameCheckReqDto reqDto) {
        return userService.usernameCheck(reqDto);
    }

    // 이메일 증복 확인
    @PostMapping("/user/email-check")
    public ResponseEntity<? super EmailCheckRspDto> emailCheck(@RequestBody @Valid EmailCheckReqDto reqDto) {
        return userService.emailCheck(reqDto);
    }

    // 인증번호 메일 전송
    @PostMapping("/user/email-certification")
    public ResponseEntity<? super EmailCertificationRspDto> emailCertification(@RequestBody @Valid EmailCertificationReqDto reqDto) {
        return userService.emailCertification(reqDto);
    }

    // 인증번호 확인
    @PostMapping("/user/check-certification")
    public ResponseEntity<? super CheckCertificationRspDto> checkCertification(@RequestBody @Valid CheckCertificationReqDto reqDto) {
        return userService.checkCertification(reqDto);
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@ModelAttribute RegisterReqDto reqDto) {
        return userService.joinUser(reqDto);
    }

    // 아이디 찾기
    @PostMapping("/idform")
    @ResponseBody
    public ResponseEntity<String> findId(@RequestBody FindIdReqDto reqDto) {
        try {
            String username = userService.findUserId(reqDto);
            return ResponseEntity.ok(username);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
}