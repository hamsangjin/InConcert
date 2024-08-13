package com.inconcert.domain.post.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.CategoryRepository;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.notification.service.NotificationService;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.util.DateUtil;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    // postId를 가지고 게시물을 조회해서 postDto을 리턴해주는 메소드
    @Transactional
    public PostDto getPostById(Long postId) {
        Post findPost = reviewRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        // viewCount 증가
        findPost.incrementViewCount();
        Post post = reviewRepository.save(findPost);

        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .postCategory(post.getPostCategory())
                .nickname(post.getUser().getNickname())
                .viewCount(post.getViewCount())
                .matchCount(post.getMatchCount())
                .endDate(post.getEndDate())
                .commentCount(post.getComments().size())
                .comments(post.getComments())
                .likeCount(post.getLikes().size())
                .isNew(Duration.between(post.getCreatedAt(), LocalDateTime.now()).toDays() < 1)
                .createdAt(post.getCreatedAt())
                .user(post.getUser())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PostDto> findByKeywordAndFilters(String keyword, String period, String type) {
        LocalDateTime startDate = DateUtil.getStartDate(period);
        LocalDateTime endDate = DateUtil.getCurrentDate();

        // 검색 로직 구현 (기간 필터링, 타입 필터링 등)
        List<Post> posts = reviewRepository.findByKeywordAndFilters(keyword, startDate, endDate, type);
        List<PostDto> postDtos = getPostDtos(posts);

        return postDtos;
    }

    @Transactional
    public void save(PostDto postDto){

        // 게시물 작성 폼에서 가져온 postCategory 제목으로 조회해서 PostCategory 리스트 생성
        List<PostCategory> postCategories = postCategoryRepository.findByTitle(postDto.getPostCategoryTitle());

        // 게시물 작성 폼에서 가져온 Category 제목으로 조회해서 Category 객체 생성
        Category category = categoryRepository.findByTitle(postDto.getCategoryTitle())
                .orElseThrow(() -> new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage()));

        // 적절한 PostCategory 찾기
        PostCategory postCategory = postCategories.stream()
                .filter(pc -> pc.getCategory().equals(category))
                .findFirst()
                .orElseThrow(() -> new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_NOT_FOUND.getMessage()));

        // 생성한 Category를 builder를 통해 연관관계 주입
        PostCategory updatedPostCategory = postCategory.builder()
                .id(postCategory.getId())
                .title(postCategory.getTitle())
                .category(category)
                .build();

        postDto.setUser(userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage())));

        // 주입된 PostCategory를 Post에 저장
        Post post = PostDto.toEntity(postDto, updatedPostCategory);

        reviewRepository.save(post);

        // 알림 생성 로직 추가
        notificationService.publishNotification(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = reviewRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
        reviewRepository.delete(post);
    }

    private static List<PostDto> getPostDtos(List<Post> posts) {
        List<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {
            PostDto postDto = PostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .thumbnailUrl(post.getThumbnailUrl())
                    .postCategory(post.getPostCategory())
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