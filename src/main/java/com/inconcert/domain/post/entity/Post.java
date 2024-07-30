package com.inconcert.domain.post.entity;

import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.view.entity.View;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column
    private Boolean isEnd = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post")
    private Set<Comment> comments;

    @OneToMany(mappedBy = "post")
    private Set<Like> likes;

    @OneToMany(mappedBy = "post")
    private Set<View> views;
}