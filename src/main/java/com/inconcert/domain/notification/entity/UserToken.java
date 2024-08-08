package com.inconcert.domain.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_tokens")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserToken {

    @Id
    private Long userId;

    private String token;
}