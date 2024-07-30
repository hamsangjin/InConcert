package com.inconcert.domain.chat.entity;

import com.inconcert.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chats")
@Getter
@NoArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @ManyToOne
    @JoinColumn(name = "guest_user_id", nullable = false)
    private User guestUser;

    @Column
    private String message;

}