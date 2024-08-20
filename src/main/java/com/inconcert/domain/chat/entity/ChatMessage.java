package com.inconcert.domain.chat.entity;

import com.inconcert.domain.user.entity.User;
import com.inconcert.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "longtext")
    private String message;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender, String message, String timestamp) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }
}