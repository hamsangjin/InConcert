package com.inconcert.domain.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long chatRoomId;
    private String username;
    private String nickname;
    private String message;
    private LocalDateTime createdAt;
    private String profileImage;
    private MessageType type;
    private boolean isNotice;

    public enum MessageType {
        ENTER, CHAT, LEAVE
    }
}