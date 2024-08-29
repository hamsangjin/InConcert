package com.inconcert.domain.user.controller;

import com.inconcert.domain.user.dto.request.*;
import com.inconcert.domain.user.dto.response.*;
import com.inconcert.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserApiController {
    private final UserService userService;

    // 로그인
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LogInReqDto reqDto, HttpServletResponse response) {
        return userService.login(reqDto, response);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return userService.getRefreshToken(request, response);
    }

    // 아이디 중복 확인
    @PostMapping("/user/id-check")
    public ResponseEntity<? super UsernameCheckRspDto> usernameCheck(@RequestBody @Valid UsernameCheckReqDto reqDto) {
        return userService.checkUsername(reqDto);
    }

    // 이메일 증복 확인
    @PostMapping("/user/email-check")
    public ResponseEntity<? super EmailCheckRspDto> emailCheck(@RequestBody @Valid EmailCheckReqDto reqDto) {
        return userService.checkEmail(reqDto);
    }

    // 닉네임 중복 확인
    @PostMapping("/user/nickname-check")
    public ResponseEntity<? super NicknameCheckRspDto> nicknameCheck(@RequestBody @Valid NicknameCheckReqDto reqDto) {
        return userService.checkNickname(reqDto);
    }

    // 전화번호 중복 확인
    @PostMapping("/user/phone-number-check")
    public ResponseEntity<? super PhoneNumberCheckRspDto> phoneNumberCheck(@RequestBody @Valid PhoneNumberCheckReqDto reqDto) {
        return userService.checkPhoneNumber(reqDto);
    }

    // 인증번호 메일 전송
    @PostMapping("/user/email-certification")
    public ResponseEntity<? super EmailCertificationRspDto> emailCertification(@RequestBody @Valid EmailCertificationReqDto reqDto) {
        return userService.sendCertificationNumber(reqDto);
    }

    // 인증번호 확인
    @PostMapping("/user/check-certification")
    public ResponseEntity<? super CheckCertificationRspDto> checkCertification(@RequestBody @Valid CheckCertificationReqDto reqDto) {
        return userService.checkCertification(reqDto);
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReqDto reqDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMap.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errorMap);
        }
        return userService.joinUser(reqDto);
    }

    // 아이디 찾기
    @PostMapping("/idform")
    public ResponseEntity<String> findId(@RequestBody FindIdReqDto reqDto) {
        return userService.findUserId(reqDto);
    }

    // 비밀번호 찾기
    @PostMapping("/findpw")
    public ResponseEntity<String> findId(@RequestBody FindPasswordReqDto reqDto) {
        return userService.findPassword(reqDto);
    }

    // 벤 날짜 반환
    @GetMapping("/api/user/{username}/banDate")
    public ResponseEntity<?> getBanDate(@PathVariable("username") String username) {
        return ResponseEntity.ok(Map.of("banDate", userService.getUserByUsername(username).getBanDate()));
    }
}
