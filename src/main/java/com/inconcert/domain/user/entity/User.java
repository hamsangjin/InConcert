package com.inconcert.domain.user.entity;

import com.inconcert.domain.chat.entity.Chat;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.user.dto.request.MyPageEditReqDto;
import com.inconcert.global.auth.jwt.token.entity.Token;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false, name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate birth;

    @Column
    private String profileImage = "/images/profile.png";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column
    private Double mannerPoint = 5.0;

    @Column
    private String intro = "안녕하세요";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mbti mbti;

    @Column
    private Integer point = 10;

    // ----------------------------

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Token> tokens;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "hostUser")
    private Set<Chat> hostChattings;

    @OneToMany(mappedBy = "guestUser")
    private Set<Chat> guestChattings;


    @Builder
    public User(String username, String password, String email, String name, String nickname, String phoneNumber,
                LocalDate birth, String profileImage, Gender gender, Double mannerPoint, String intro, Mbti mbti, Integer point, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
        this.profileImage = profileImage != null ? profileImage : this.profileImage;
        this.gender = gender;
        this.mannerPoint = mannerPoint != null ? mannerPoint : this.mannerPoint;
        this.intro = intro != null ? intro : this.intro;
        this.mbti = mbti;
        this.point = point != null ? point : this.point;
        this.roles = roles;
    }

    // 임시 비밀번호로 업데이트
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    // 유저 정보 수정
    public void updateUser(MyPageEditReqDto reqDto, String password, String profileImageUrl) {
        this.nickname = reqDto.getNickname();
        this.password = password;
        this.email = reqDto.getEmail();
        this.phoneNumber = reqDto.getPhoneNumber();
        this.mbti = reqDto.getMbti();
        this.intro = reqDto.getIntro();
        this.profileImage = profileImageUrl;
    }
}