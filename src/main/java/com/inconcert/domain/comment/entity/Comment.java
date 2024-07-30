package com.inconcert.domain.comment.entity;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor
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

    @Column
    private Boolean isSecret = false;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private Set<Comment> replies;
}