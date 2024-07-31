package com.inconcert.domain.comment.dto;

import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 댓글 생성 폼 DTO
public class CommentCreateForm {
    private Long id;
    private User user;
    private Post post;
    private boolean isSecret;

    @NotEmpty(message = "내용은 필수항목입니다.")
    private String content;

    public Comment toEntity() {
        Comment comment = Comment.builder()
                .id(getId())
                .user(getUser())
                .post(getPost())
                .content(getContent())
                .isSecret(isSecret())
                .build();
        return comment;
    }
}