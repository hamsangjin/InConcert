package com.inconcert.domain.post.service;

import com.inconcert.domain.crawling.service.PerformanceService;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.util.DateUtil;
import com.inconcert.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InfoService {
    private final InfoRepository infoRepository;
    private final PerformanceService performanceService;

    public List<PostDto> getAllInfoPostsByPostCategory(String postCategoryTitle) {
        List<Post> posts = switch (postCategoryTitle) {
            case "musical" -> infoRepository.findPostsByPostCategoryTitle("musical");
            case "concert" -> infoRepository.findPostsByPostCategoryTitle("concert");
            case "theater" -> infoRepository.findPostsByPostCategoryTitle("theater");
            case "etc" -> infoRepository.findPostsByPostCategoryTitle("etc");
            default -> throw new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_NOT_FOUND.getMessage());
        };
        return getPostDtos(posts);
    }

    public Post getPostByPostId(Long postId) {
        Optional<Post> post = infoRepository.findById(postId);
        return post.orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }

    // postId를 가지고 게시물을 조회해서 postDto을 리턴해주는 메소드
    @Transactional
    public PostDto getPostDtoByPostId(Long postId) {
        Post findPost = infoRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        // viewCount 증가
        findPost.incrementViewCount();
        Post post = infoRepository.save(findPost);

        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .thumbnailUrl(post.getThumbnailUrl())  // 썸네일 URL 추가
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

    public List<PostDto> findByKeywordAndFilters(String postCategoryTitle, String keyword, String period, String type) {
        LocalDateTime startDate = DateUtil.getStartDate(period);
        LocalDateTime endDate = DateUtil.getCurrentDate();

        // 검색 로직 구현 (기간 필터링, 타입 필터링 등)
        List<Post> posts = infoRepository.findByKeywordAndFilters(postCategoryTitle, keyword, startDate, endDate, type);

        return getPostDtos(posts);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = infoRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
        infoRepository.delete(post);
    }

    @Transactional
    public void crawlAndSavePosts(String type) {
        performanceService.crawlPerformances(type);
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