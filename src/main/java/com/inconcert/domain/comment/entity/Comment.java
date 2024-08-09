package com.inconcert.domain.comment.entity;

import com.inconcert.domain.comment.dto.CommentDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import com.inconcert.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    @Column(name = "is_secret")
    private Boolean isSecret = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    // 댓글 수정
    public void update(String content, Boolean isSecret) {
        this.content = content;
        this.isSecret = isSecret;
    }

    public void confirmPost(Post post) {
        this.post = post;
        post.addComment(this);
    }

    public void addReply(Comment reply) {
        replies.add(reply);
    }

    public void confirmParent(Comment parent) {
        this.parent = parent;
        parent.addReply(this);
    }

    public CommentDto toCommentDto() {
        return CommentDto.builder()
                .id(id)
                .user(user)
                .post(post)
                .content(content)
                .isSecret(isSecret)
                .parent(parent)
                .replies(replies)
                .build();
    }
}
