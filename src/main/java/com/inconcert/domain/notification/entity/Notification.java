package com.inconcert.domain.notification.entity;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import com.inconcert.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String keyword;

    @Column(nullable = false, columnDefinition = "longtext")
    private String message;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(nullable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder
    public Notification(String keyword, String message, boolean isRead, User user, Post post, String type) {
        this.keyword = keyword;
        this.message = message;
        this.isRead = isRead;
        this.user = user;
        this.post = post;
        this.type = type;
    }
}

