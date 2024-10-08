package com.inconcert.domain.chat.entity;

import com.inconcert.domain.user.entity.User;
import com.inconcert.common.entity.BaseEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "longtext")
    private String message;

    @Column(name = "is_notice", nullable = false)
    private boolean isNotice = false;

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender,
                       String message, boolean isNotice) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
        this.isNotice = isNotice;
    }
}