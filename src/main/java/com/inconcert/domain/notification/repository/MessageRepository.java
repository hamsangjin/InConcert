package com.inconcert.domain.notification.repository;

import com.inconcert.domain.notification.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByUserIdOrderByIsReadAsc(Long userId);
}