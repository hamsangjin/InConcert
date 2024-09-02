package com.inconcert.domain.chat.entity;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id")
    private User hostUser;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "post_id")
    private Post post;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomUser> users = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatNotification> notifications = new ArrayList<>();

    @Builder
    public ChatRoom(String roomName, User hostUser, Post post, List<ChatRoomUser> users) {
        this.roomName = roomName;
        this.hostUser = hostUser;
        this.post = post;
        this.users = users != null ? users : new ArrayList<>();
    }

    public void addUser(User user) {
        ChatRoomUser chatRoomUser = ChatRoomUser.builder()
                .user(user)
                .chatRoom(this)
                .build();
        this.users.add(chatRoomUser);
    }

    public void assignPost(Post post) {
        this.post = post;
        post.assignChatRoom(this);
    }
}