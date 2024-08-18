package com.inconcert.domain.chat.dto;

import com.inconcert.domain.chat.entity.ChatNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationMessage {
    private Long id;
    private String message;
    private Long chatRoomId;
    private Long userId;

    public NotificationMessage(ChatNotification chatNotification) {
        this.id = chatNotification.getId();
        this.message = chatNotification.getMessage();
        this.chatRoomId = chatNotification.getChatRoom().getId();
        this.userId = chatNotification.getUser().getId();
    }
}
