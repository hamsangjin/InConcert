package com.inconcert.global.auth.jwt.token.service;

import com.inconcert.global.auth.jwt.token.entity.Token;
import com.inconcert.global.auth.jwt.token.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

    @Transactional
    public Token saveToken(Token token) {
        return tokenRepository.save(token);
    }

}
