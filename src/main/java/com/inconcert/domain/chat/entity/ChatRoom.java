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

    @ManyToOne
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @ManyToMany
    @JoinTable(
            name = "chat_room_users",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )

    private List<User> users = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;



    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @Builder
    public ChatRoom(String roomName,Post post, User hostUser, List<User> users) {
        this.roomName = roomName;
        this.post = post;
        this.hostUser = hostUser;
        this.users = users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public void assignPost(Post post) {
        this.post = post;
        post.assignChatRoom(this);
    }
}