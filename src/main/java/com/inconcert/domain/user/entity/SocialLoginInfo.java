package com.inconcert.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "social_login_info")
@Getter
@NoArgsConstructor
public class SocialLoginInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String socialId;

    @Column(nullable = false)
    private String uuid;
}