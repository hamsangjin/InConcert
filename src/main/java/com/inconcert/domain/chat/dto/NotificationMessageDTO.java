package com.inconcert.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationMessageDTO {
    private Long id;
    private String message;
    private Long chatRoomId;
    private Long userId;
}
