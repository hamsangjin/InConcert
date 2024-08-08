package com.inconcert.domain.notification.service;

import com.inconcert.domain.notification.entity.UserToken;
import com.inconcert.domain.notification.repository.UserTokenRepository;
import com.inconcert.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserTokenService {
    private final UserTokenRepository userTokenRepository;
    private final UserRepository userRepository;

    public void saveToken(Long userId, String token) {
        UserToken userToken = new UserToken(userId, token);
        userTokenRepository.save(userToken);
    }

    public String getTokenByUserId(Long userId) {
        System.out.println("getTokenByUserId 호출");
        return userTokenRepository.findById(userId)
                .map(UserToken::getToken)
                .orElse(null);
    }
}