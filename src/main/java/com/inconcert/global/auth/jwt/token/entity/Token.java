package com.inconcert.global.auth.jwt.token.entity;

import com.inconcert.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tokens")
@Getter
@NoArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "access_token_value", nullable = false)
    private String accessTokenValue;

    @Column(name = "refresh_token_value", nullable = false)
    private String refreshTokenValue;

    @Builder
    public Token(String accessTokenValue, String refreshTokenValue, User user) {
        this.accessTokenValue = accessTokenValue;
        this.refreshTokenValue = refreshTokenValue;
        this.user = user;
    }
}