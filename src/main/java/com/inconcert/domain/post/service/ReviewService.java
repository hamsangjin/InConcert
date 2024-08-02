package com.inconcert.domain.post.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.CategoryRepository;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.global.exception.CategoryNotFoundException;
import com.inconcert.global.exception.PostCategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void save(PostDto postDto){
        // 게시물 작성 폼에서 가져온 postCategory 제목으로 조회해서 PostCategory 객체 생성
        PostCategory postCategory = postCategoryRepository.findByTitle(postDto.getPostCategoryTitle())
                .orElseThrow(() -> new PostCategoryNotFoundException(postDto.getPostCategoryTitle() + "라는 Post Category를 찾지 못했습니다."));

        // 게시물 작성 폼에서 가져온 Category 제목으로 조회해서 Category 객체 생성
        Category category = categoryRepository.findById(postDto.getPostCategory().getId())
                .orElseThrow(() -> new CategoryNotFoundException(postDto.getCategoryTitle() + "라는 Category를 찾지 못했습니다."));

        // 생성한 Category를 builder를 통해 연관관계 주입
        PostCategory updatedPostCategory = postCategory.builder()
                .id(postCategory.getId())
                .title(postCategory.getTitle())
                .category(category)
                .build();

        // 주입된 PostCategory를 Post에 저장
        Post post = PostDto.toEntity(postDto, updatedPostCategory);

        reviewRepository.save(post);
    }
}
