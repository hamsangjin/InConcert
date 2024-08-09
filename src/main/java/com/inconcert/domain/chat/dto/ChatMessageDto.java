package com.inconcert.domain.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageDto {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String message;
    private String timestamp;
}