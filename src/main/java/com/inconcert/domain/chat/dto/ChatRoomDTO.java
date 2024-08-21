package com.inconcert.domain.chat.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private String roomName;
    private Long hostUserId;
    private int userCount;
    private String timeSince;
    private int diffTime;
}
