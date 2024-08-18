package com.inconcert.domain.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageDto {
    private Long id;
    private Long chatRoomId;
    private String username;
    private String message;
    private String timestamp;
    private MessageType type;

    public enum MessageType {
        ENTER, CHAT, LEAVE;
    }
}
