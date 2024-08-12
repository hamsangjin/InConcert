package com.inconcert.domain.notification.entity;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import com.inconcert.global.entity.BaseEntity;
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

    @Column
    private String message;

    @Column
    private boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder
    public Notification(String keyword, String message, boolean isRead, User user, Post post) {
        this.keyword = keyword;
        this.message = message;
        this.isRead = isRead;
        this.user = user;
        this.post = post;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}

