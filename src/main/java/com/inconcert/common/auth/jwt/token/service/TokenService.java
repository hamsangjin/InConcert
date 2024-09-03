package com.inconcert.common.auth.jwt.token.service;

import com.inconcert.common.auth.jwt.token.entity.Token;
import com.inconcert.common.auth.jwt.token.repository.TokenRepository;
import com.inconcert.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

    @Transactional
    public Token saveToken(Token token) {
        return tokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public Optional<Token> getTokenByUser(User user) {
        return tokenRepository.findByUser(user);
    }
}
