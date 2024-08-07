package com.inconcert.domain.user.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.repository.MyPageRepostory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final MyPageRepostory myPageRepostory;

    @Transactional(readOnly = true)
    public List<PostDto> mypageBoard(Long userId) {
        List<Post> posts = myPageRepostory.findByUserId(userId);
        return getPostDtos(posts);
    }

    @Transactional(readOnly = true)
    public List<PostDto> mypageComment(Long userId) {
        List<Post> posts = myPageRepostory.findPostsWithMyComments(userId);
        return getPostDtos(posts);
    }

    @Transactional(readOnly = true)
    public List<PostDto> mypageLike(Long userId) {
        List<Post> posts = myPageRepostory.findPostsILiked(userId);
        return getPostDtos(posts);
    }

    private static List<PostDto> getPostDtos(List<Post> posts) {
        List<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {

            PostCategory postCategory = post.getPostCategory();
            Category category = postCategory.getCategory();

            PostDto postDto = PostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .category(category)
                    .postCategory(postCategory)
                    .thumbnailUrl(post.getThumbnailUrl())
                    .nickname(post.getUser().getNickname())
                    .viewCount(post.getViewCount())
                    .commentCount(post.getComments().size())
                    .likeCount(post.getLikes().size())
                    .isNew(Duration.between(post.getCreatedAt(), LocalDateTime.now()).toDays() < 1)
                    .createdAt(post.getCreatedAt())
                    .build();

            postDtos.add(postDto);
        }
        return postDtos;
    }
}
