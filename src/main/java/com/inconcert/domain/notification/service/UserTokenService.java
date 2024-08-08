package com.inconcert.domain.notification.service;

import com.inconcert.domain.notification.entity.UserToken;
import com.inconcert.domain.notification.repository.UserTokenRepository;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        return userTokenRepository.findById(userId)
                .map(UserToken::getToken)
                .orElse(null);
    }

    public List<String> getTokensByKeywords(List<String> keywords) {
        return userRepository.findAll().stream()
                .filter(user -> user.getKeywords().stream().anyMatch(keywords::contains))
                .map(User::getId)
                .map(this::getTokenByUserId)
                .collect(Collectors.toList());
    }
}