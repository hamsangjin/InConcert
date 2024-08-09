package com.inconcert.domain.chat.dto;



import com.inconcert.domain.chat.entity.ChatRoom;
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