package com.inconcert.domain.chat.entity;

import com.inconcert.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_notifications")
@Getter
@NoArgsConstructor
public class ChatNotification { // 채팅 관련 알림
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "request_user_id")
    private User requestUser;

    @Builder
    public ChatNotification(String message, ChatRoom chatRoom, User user, User requestUser) {
        this.message = message;
        this.chatRoom = chatRoom;
        this.user = user;
        this.requestUser = requestUser;
    }
}
