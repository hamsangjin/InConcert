package com.inconcert.common.auth.jwt.token.repository;

import com.inconcert.common.auth.jwt.token.entity.Token;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByUser(User user);
}
