package com.inconcert.domain.chat.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
