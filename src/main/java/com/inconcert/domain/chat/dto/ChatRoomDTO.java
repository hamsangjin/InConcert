package com.inconcert.domain.chat.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private String roomName;
    private Long hostUserId;
    private int userCount;
    private String timeSince;
    private int diffTime;
    private Long postId;

    public ChatRoomDTO(Long id, String roomName, Long hostUserId, int userCount, Long postId) {
        this.id = id;
        this.roomName = roomName;
        this.hostUserId = hostUserId;
        this.userCount = userCount;
        this.postId = postId;
    }
}