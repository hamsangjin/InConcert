package com.inconcert.domain.chat.repository;

import com.inconcert.domain.chat.dto.ChatMessageDTO;
import com.inconcert.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT new com.inconcert.domain.chat.dto.ChatMessageDTO(" +
            "cm.id, cm.chatRoom.id, cm.sender.username, cm.sender.nickname, cm.message, " +
            "cm.createdAt, cm.sender.profileImage, com.inconcert.domain.chat.dto.ChatMessageDTO.MessageType.CHAT, cm.isNotice) " +
            "FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id = :chatRoomId " +
            "ORDER BY cm.createdAt ASC")
    List<ChatMessageDTO> findByChatRoom(@Param("chatRoomId") Long chatRoomId);
}