package com.inconcert.domain.post.entity;

import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.user.entity.User;
import com.inconcert.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "match_count", nullable = false)
    private int matchCount;

//    @Column(name = "thumbnail_url")
//    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post")
    private Set<Comment> comments;

    @OneToMany(mappedBy = "post")
    private Set<Like> likes;

    @Column(name = "view_count")
    private int viewCount = 0;

    @ManyToOne
    @JoinColumn(name = "post_category_id", nullable = false)
    private PostCategory postCategory;
}