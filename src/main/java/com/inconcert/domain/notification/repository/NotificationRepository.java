package com.inconcert.domain.notification.repository;

import com.inconcert.domain.notification.entity.Notification;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndIsReadFalse(User user);
}

