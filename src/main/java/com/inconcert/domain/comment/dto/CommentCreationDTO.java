package com.inconcert.domain.comment.dto;

import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentCreationDTO {
    private Long id;
    private User user;
    private Post post;
    private boolean isSecret;
    private Long parent;

    @NotEmpty(message = "내용은 필수항목입니다.")
    private String content;

    public Comment toEntity() {
        Comment comment = Comment.builder()
                .id(getId())
                .user(getUser())
                .post(getPost())
                .content(getContent())
                .isSecret(getIsSecret())
                .build();
        return comment;
    }

    public Boolean getIsSecret() {
        return isSecret;
    }

    public void setIsSecret(Boolean isSecret) {
        this.isSecret = isSecret;
    }
}
