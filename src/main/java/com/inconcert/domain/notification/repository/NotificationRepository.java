package com.inconcert.domain.notification.repository;

import com.inconcert.domain.notification.dto.NotificationDTO;
import com.inconcert.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 전체 알림 불러오기(안 읽은 순 -> 최신 순)
    @Query("SELECT new com.inconcert.domain.notification.dto.NotificationDTO(n.id, n.keyword, n.message, n.isRead," +
            "n.type, n.createdAt, c.title, pc.title, p.id) " +
            "FROM Notification n " +
            "JOIN n.post p " +
            "JOIN n.post.postCategory pc " +
            "JOIN n.post.postCategory.category c " +
            "WHERE n.user.id = :userId " +
            "ORDER BY n.isRead Asc, n.createdAt Desc")
    List<NotificationDTO> findByUserOrderByIsReadAscCreatedAtDesc(@Param("userId") Long userId);


    // 타입에 따라 알림 불러오기
    @Query("SELECT new com.inconcert.domain.notification.dto.NotificationDTO(n.id, n.keyword, n.message, n.isRead," +
            "n.type, n.createdAt, c.title, pc.title, p.id) " +
            "FROM Notification n " +
            "JOIN n.post p " +
            "JOIN n.post.postCategory pc " +
            "JOIN n.post.postCategory.category c " +
            "WHERE n.user.id = :userId AND n.type = :type " +
            "ORDER BY n.isRead Asc, n.createdAt Desc")
    List<NotificationDTO> findByTypeAndUserIdOrderByIsReadAscCreatedAtDesc(@Param("type") String type, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.type = 'likes' AND n.user.id = :userId AND n.post.id = :postId")
    void deleteByTypeAndUserIdAndPostId(@Param("userId") Long userId, @Param("postId")Long postId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);
}