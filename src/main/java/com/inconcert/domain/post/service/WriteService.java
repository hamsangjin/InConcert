package com.inconcert.domain.post.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.CategoryRepository;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.notification.service.NotificationService;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.CategoryNotFoundException;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.PostCategoryNotFoundException;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WriteService {
    private final PostCategoryRepository postCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final InfoRepository infoRepository;
    private final MatchRepository matchRepository;
    private final ReviewRepository reviewRepository;
    private final TransferRepository transferRepository;
    private final NotificationService notificationService;

    @Transactional
    public Post save(PostDto postDto){

        // 게시물 작성 폼에서 가져온 postCategory 제목으로 조회해서 PostCategory 리스트 생성
        List<PostCategory> postCategories = postCategoryRepository.findByTitle(postDto.getPostCategoryTitle());

        // 게시물 작성 폼에서 가져온 Category 제목으로 조회해서 Category 객체 생성
        Category category = categoryRepository.findByTitle(postDto.getCategoryTitle())
                .orElseThrow(() -> new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage()));

        // 적절한 PostCategory 찾기
        PostCategory postCategory = postCategories.stream()
                .filter(pc -> pc.getCategory().equals(category))
                .findFirst()
                .orElseThrow(() -> new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_COMBINATION_NOT_FOUND.getMessage()));

        // 생성한 Category를 builder를 통해 연관관계 주입
        PostCategory updatedPostCategory = postCategory.builder()
                .id(postCategory.getId())
                .title(postCategory.getTitle())
                .category(category)
                .build();

        postDto.setUser(userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage())));

        // 동행을 제외한 카테고리들은 모집인원, 마감 날짜 제거
        if(!category.getTitle().equals("match")){
            postDto.setMatchCount(0);
            postDto.setEndDate(null);
        }

        // 주입된 PostCategory를 Post에 저장
        Post post = PostDto.toEntity(postDto, updatedPostCategory);

        Post savePost = switch (category.getTitle()) {
            case "info" -> infoRepository.save(post);
            case "review" -> reviewRepository.save(post);
            case "match" -> matchRepository.save(post);
            case "transfer" -> transferRepository.save(post);
            default -> throw new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage());
        };

        // 알림 생성 로직 추가
        notificationService.keywordsNotification(post);

        return savePost;
    }
}