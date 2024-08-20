package com.inconcert.domain.post.entity;

import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.notification.entity.Notification;
import com.inconcert.domain.user.entity.User;
import com.inconcert.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

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

    @Lob
    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "match_count")
    private int matchCount;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "view_count")
    private int viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_category_id", nullable = false)
    private PostCategory postCategory;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @OneToOne(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private ChatRoom chatRoom;

    public void incrementViewCount() {
        this.viewCount += 1;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void assignChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public boolean hasChatRoom() {
        return this.chatRoom != null;
    }
}