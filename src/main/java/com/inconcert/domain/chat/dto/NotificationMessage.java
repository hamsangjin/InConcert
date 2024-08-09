package com.inconcert.domain.chat.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationMessage {

    private String message;
    private Long chatRoomId;
}
