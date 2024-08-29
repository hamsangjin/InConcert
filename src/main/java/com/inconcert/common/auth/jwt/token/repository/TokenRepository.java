package com.inconcert.common.auth.jwt.token.repository;

import com.inconcert.common.auth.jwt.token.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
}
