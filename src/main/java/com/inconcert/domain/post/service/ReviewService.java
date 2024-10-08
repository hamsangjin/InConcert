package com.inconcert.domain.post.service;

import com.inconcert.common.service.ImageService;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.util.DateUtils;
import com.inconcert.common.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public Page<PostDTO> getAllInfoPostsByPostCategory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findPostsByPostCategoryTitle(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getByKeywordAndFilters(String keyword, String period, String type, int page, int size) {
        LocalDateTime startDate = DateUtils.getStartDate(period);
        LocalDateTime endDate = DateUtils.getCurrentDate();
        Pageable pageable = PageRequest.of(page, size);

        // 데이터베이스에서 조건에 맞는 게시물 검색
        return reviewRepository.findByKeywordAndFilters(keyword, startDate, endDate, type, pageable);
    }

    // postId를 가지고 게시물을 조회해서 postDto을 리턴해주는 메소드
    @Transactional
    public PostDTO getPostDtoByPostId(Long postId) {
        Post findPost = reviewRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        if(findPost.isEnd()){
            return null;
        }

        // viewCount 증가
        findPost.incrementViewCount();
        Post post = reviewRepository.save(findPost);

        return PostDTO.builder()
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

    @Transactional
    public void deletePost(Long postId) {
        Post post = reviewRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        // 포스트에 포함된 모든 이미지 삭제
        List<String> imageKeys = imageService.extractImageKeys(post.getContent());
        for (String key : imageKeys) {
            imageService.deleteImage(key);
        }

        reviewRepository.delete(post);
    }
}