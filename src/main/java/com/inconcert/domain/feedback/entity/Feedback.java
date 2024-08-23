package com.inconcert.domain.feedback.entity;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int point;

    @ManyToOne(fetch = FetchType.LAZY)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    private User reviewee;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public Feedback(int point, User reviewer, User reviewee, Post post) {
        this.point = point;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.post = post;
    }
}