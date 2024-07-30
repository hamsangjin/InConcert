package com.inconcert.domain.comment.dto;

import com.inconcert.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 응답용 댓글 DTO
public class CommentResponseDto {
    private Long id;
    private String nickname;
    private Long postId;
    private String content;
    private LocalDateTime updatedAt = LocalDateTime.now().withNano(0);
    private boolean isSecret;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.nickname = comment.getUser().getUsername();
        this.postId = comment.getPost().getId();
        this.content = comment.getContent();
        this.updatedAt = comment.getUpdatedAt();
        this.isSecret = comment.getIsSecret();
    }
}