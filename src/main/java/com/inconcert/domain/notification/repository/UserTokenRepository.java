package com.inconcert.domain.notification.repository;

import com.inconcert.domain.notification.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    // 필요한 경우 커스텀 쿼리 메서드를 여기에 추가할 수 있습니다.
}