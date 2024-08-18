package com.inconcert.domain.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomDto {
    private Long id;
    private String roomName;
    private Long hostUserId;
    private int userCount;
}
