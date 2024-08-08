package com.inconcert.domain.notification.controller;

import com.inconcert.domain.notification.service.UserTokenService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TokenController {
    private final UserTokenService userTokenService;

    @PostMapping("/token")
    public void registerToken(@RequestBody TokenRequest tokenRequest) {
        System.out.println("토큰 등록 요청 받음: " + tokenRequest.getToken());
        userTokenService.saveToken(tokenRequest.getUserId(), tokenRequest.getToken());
    }

    @Getter
    private static class TokenRequest {
        private Long userId;
        private String token;
    }
}