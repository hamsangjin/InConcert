package com.inconcert.domain.post.service;

import com.inconcert.common.service.ImageService;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.util.DateUtils;
import com.inconcert.common.exception.*;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
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

    // 수정 필요
    @Transactional
    public void deletePost(Long postId) {
        Post post = reviewRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        // 포스트에 포함된 모든 이미지 삭제
        List<String> imageKeys = extractImageKeys(post.getContent());
        for (String key : imageKeys) {
            imageService.deleteImage(key);
        }

        reviewRepository.delete(post);
    }

    private List<String> extractImageKeys(String content) {
        List<String> imageKeys = new ArrayList<>();

        // Jsoup을 사용하여 HTML 파싱
        Document document = Jsoup.parse(content);

        // <img> 태그를 모두 선택
        Elements imgElements = document.select("img");

        // 각 <img> 태그의 src 속성에서 S3 키 추출
        for (Element img : imgElements) {
            String imageUrl = img.attr("src");

            // S3 키 추출: URL에서 마지막 '/' 이후의 부분이 파일 이름 (S3 키)라고 가정
            String imageKey = extractKeyFromUrl(imageUrl);

            if (imageKey != null) {
                imageKeys.add(imageKey);
            }
        }

        return imageKeys;
    }

    private String extractKeyFromUrl(String url) {
        // 예: https://d12345678abcdef.cloudfront.net/your-bucket-name/image12345.png
        // 위 URL에서 "image12345.png" 추출
        try {
            int lastSlashIndex = url.lastIndexOf('/');
            if (lastSlashIndex != -1) {
                return url.substring(lastSlashIndex + 1);
            }
        } catch (Exception e) {
            // URL이 예상과 다를 경우 null 반환
            return null;
        }
        return null;
    }
}