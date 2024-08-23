package com.inconcert.domain.post.dto;

import com.inconcert.domain.category.entity.*;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import lombok.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
    private Long id;
    private String title;
    private Category category;
    private String categoryTitle;
    private PostCategory postCategory;
    private String postCategoryTitle;
    private LocalDate endDate;
    private Long chatRoomId;
    private int chatRoomUserSize;
    private int matchCount;
    private String content;
    private String thumbnailUrl;
    private String username;
    private String nickname;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private List<Comment> comments;
    private boolean isNew;
    private LocalDateTime createdAt;
    private User user;
    private boolean isEnd;

    // post 저장
    public static Post toEntity(PostDTO postDto, PostCategory postCategory) {
        return Post.builder()
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .endDate(postDto.getEndDate())
                .matchCount(postDto.getMatchCount())
                .thumbnailUrl(postDto.getThumbnailUrl())
                .comments(new ArrayList<>())
                .likes(new ArrayList<>())
                .postCategory(postCategory)
                .user(postDto.getUser())
                .build();
    }

    public PostDTO(Long id, String title, String categoryTitle, String postCategoryTitle, String thumbnailUrl,
                   String nickname, int viewCount, int likeCount, int commentCount, boolean isNew, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.categoryTitle = categoryTitle;
        this.postCategoryTitle = postCategoryTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.nickname = nickname;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isNew = isNew;
        this.createdAt = createdAt;
    }

    public PostDTO(Long id, String postCategoryTitle, String thumbnailUrl) {
        this.id = id;
        this.postCategoryTitle = postCategoryTitle;
        this.thumbnailUrl = thumbnailUrl;
    }
}