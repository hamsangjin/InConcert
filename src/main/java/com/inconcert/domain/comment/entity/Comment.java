package com.inconcert.domain.comment.entity;

import com.inconcert.domain.comment.dto.CommentDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "comments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column
    private Boolean isSecret = false;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> replies;

    // 댓글 수정
    public void update(String content, Boolean isSecret) {
        this.content = content;
        this.isSecret = isSecret;
        updatedAt = LocalDateTime.now().withNano(0);
    }

    public void confirmPost(Post post) {
        this.post = post;
//        post.addComment(this);
    }

    public void addReply(Comment reply) {
        replies.add(reply);
    }

    public void confirmParent(Comment parent) {
        this.parent = parent;
        parent.addReply(this);
    }

    public CommentDto toCommentDto() {
        CommentDto commentDto = CommentDto.builder()
                .id(id)
                .user(user)
                .post(post)
                .content(content)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .isSecret(isSecret)
                .replies(replies)
                .build();

        return commentDto;
    }
}