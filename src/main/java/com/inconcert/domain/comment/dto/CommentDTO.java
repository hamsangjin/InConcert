package com.inconcert.domain.comment.dto;

import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDTO {
    private Long id;
    private User user;
    private Post post;
    private String content;
    private Boolean isSecret;
    private Comment parent;
    private List<Comment> replies;
}