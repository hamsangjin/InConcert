package com.inconcert.domain.notification.repository;

import com.inconcert.domain.notification.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // 읽은 알림은 아래로, 안 읽은 알림은 최신순으로 정렬해서 message 리턴
    List<Message> findByUserIdOrderByIsReadAscCreatedAtDesc(Long userId);
}