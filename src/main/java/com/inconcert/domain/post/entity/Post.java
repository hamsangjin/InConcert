package com.inconcert.domain.post.entity;

import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.user.entity.User;
import com.inconcert.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.HashSet;
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

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "longtext")
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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    @Column(name = "view_count")
    private int viewCount = 0;

    @ManyToOne
    @JoinColumn(name = "post_category_id", nullable = false)
    private PostCategory postCategory;

    public void incrementViewCount() {
        this.viewCount += 1;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }
}