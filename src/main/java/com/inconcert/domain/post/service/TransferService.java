package com.inconcert.domain.post.service;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.TransferRepository;
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
public class TransferService {
    private final TransferRepository transferRepository;
    
    public List<PostDto> getAllTransferPostsByPostCategory(String postCategoryTitle) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        return switch (postCategoryTitle) {
            case "musical" -> transferRepository.findPostsByPostCategoryTitle("musical", yesterday);
            case "concert" -> transferRepository.findPostsByPostCategoryTitle("concert", yesterday);
            case "theater" -> transferRepository.findPostsByPostCategoryTitle("theater", yesterday);
            case "etc" -> transferRepository.findPostsByPostCategoryTitle("etc", yesterday);
            default -> throw new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_NOT_FOUND.getMessage());
        };
    }

    public Post getPostByPostId(Long postId) {
        Optional<Post> post = transferRepository.findById(postId);
        return post.orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }

    // postId를 가지고 게시물을 조회해서 postDto을 리턴해주는 메소드
    @Transactional
    public PostDto getPostDtoByPostId(Long postId) {
        Post findPost = transferRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        // viewCount 증가
        findPost.incrementViewCount();
        Post post = transferRepository.save(findPost);

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
    public List<PostDto> findByKeywordAndFilters(String postCategoryTitle, String keyword, String period, String type) {
        LocalDateTime startDate = DateUtil.getStartDate(period);
        LocalDateTime endDate = DateUtil.getCurrentDate();

        // 검색 로직 구현 (기간 필터링, 타입 필터링 등)
        List<Post> posts = transferRepository.findByKeywordAndFilters(postCategoryTitle, keyword, startDate, endDate, type);

        return getPostDtos(posts);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = transferRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("삭제하려는 게시글이 존재하지 않습니다."));
        transferRepository.delete(post);
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
