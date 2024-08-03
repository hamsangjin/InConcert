package com.inconcert.domain.post.dto;

import com.inconcert.domain.category.entity.*;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import lombok.*;
import java.time.*;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private Long id;
    private String title;
    private Category category;
    private String categoryTitle;
    private PostCategory postCategory;
    private String postCategoryTitle;
    private LocalDate endDate;
    private int matchCount;
    private String content;
    private String thumbnailUrl;
    private String username;
    private String nickname;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private Set<Comment> comments;
    private boolean isNew;
    private LocalDateTime createdAt;
    private User user;

    // post 저장
    public static Post toEntity(PostDto postDto, PostCategory postCategory) {
        return Post.builder()
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .endDate(postDto.getEndDate())
                .matchCount(postDto.getMatchCount())
//                .thumbnailUrl(postDto.getThumbnailUrl())
                .postCategory(postCategory)
                .user(postDto.getUser())
                .build();
    }

}