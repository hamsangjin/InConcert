package com.inconcert.domain.notification.repository;

import com.inconcert.domain.notification.entity.Notification;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByIsReadAscCreatedAtDesc(User user);
    List<Notification> findByTypeAndUserIdOrderByIsReadAscCreatedAtDesc(String type, Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.type = 'likes' AND n.user.id = :userId AND n.post.id = :postId")
    void deleteByTypeAndUserIdAndPostId(@Param("userId") Long userId, @Param("postId")Long postId);
}