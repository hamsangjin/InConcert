package com.inconcert.domain.user.dto.response;

import com.inconcert.domain.user.entity.User;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRspDTO {
    private Long postId;
    private Long chatRoomId;
    private String title;
    private LocalDate endDate;
    private int chatRoomUserSize;
    private int matchCount;
    private boolean isEnd;
    private String thumbnailUrl;
    private String categoryTitle;
    private String postCategoryTitle;
    private String hostNickname;
    private List<User> matchUsers;

    public MatchRspDTO(Long postId, Long chatRoomId, String title, LocalDate endDate, int chatRoomUserSize, int matchCount, boolean isEnd, String thumbnailUrl, String categoryTitle, String postCategoryTitle, String hostNickname) {
        this.postId = postId;
        this.chatRoomId = chatRoomId;
        this.title = title;
        this.endDate = endDate;
        this.chatRoomUserSize = chatRoomUserSize;
        this.matchCount = matchCount;
        this.isEnd = isEnd;
        this.thumbnailUrl = thumbnailUrl;
        this.categoryTitle = categoryTitle;
        this.postCategoryTitle = postCategoryTitle;
        this.hostNickname = hostNickname;
    }
}
