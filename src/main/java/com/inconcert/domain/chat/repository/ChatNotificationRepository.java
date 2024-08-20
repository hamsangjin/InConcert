package com.inconcert.domain.chat.repository;

import com.inconcert.domain.chat.entity.ChatNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatNotificationRepository extends JpaRepository<ChatNotification, Long> {
    List<ChatNotification> findByUserId(Long userId);
}
