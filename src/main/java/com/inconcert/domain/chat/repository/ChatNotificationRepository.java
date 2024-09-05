package com.inconcert.domain.chat.repository;

import com.inconcert.domain.chat.dto.NotificationMessageDTO;
import com.inconcert.domain.chat.entity.ChatNotification;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatNotificationRepository extends JpaRepository<ChatNotification, Long> {
    @Query("SELECT new com.inconcert.domain.chat.dto.NotificationMessageDTO(cn.id, cn.message, cn.chatRoom.id, cn.user.id) " +
            "FROM ChatNotification cn " +
            "WHERE cn.user.id = :userId " +
            "ORDER BY cn.createdAt DESC")
    List<NotificationMessageDTO> findByUserId(@Param("userId") Long userId);

    boolean existsByRequestUserAndChatRoom(User RequestUser, ChatRoom chatRoom);
}