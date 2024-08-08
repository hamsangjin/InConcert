package com.inconcert.domain.notification.repository;

import com.inconcert.domain.notification.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
}