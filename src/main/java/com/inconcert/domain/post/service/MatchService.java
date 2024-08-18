package com.inconcert.domain.post.service;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.util.DateUtil;
import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import com.inconcert.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {
    private final MatchRepository matchRepository;

    public List<PostDto> getAllMatchPostsByPostCategory(String postCategoryTitle) {
        return switch (postCategoryTitle) {
            case "musical" -> matchRepository.findPostsByPostCategoryTitle("musical");
            case "concert" -> matchRepository.findPostsByPostCategoryTitle("concert");
            case "theater" -> matchRepository.findPostsByPostCategoryTitle("theater");
            case "etc" -> matchRepository.findPostsByPostCategoryTitle("etc");
            default -> throw new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_NOT_FOUND.getMessage());
        };
    }

    public Page<PostDto> getAllInfoPostsByPostCategory(String postCategoryTitle, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return matchRepository.findPostsByPostCategoryTitle(postCategoryTitle, pageable);
    }

    public Page<PostDto> findByKeywordAndFilters(String postCategoryTitle, String keyword, String period, String type, String gender, String mbti, int page, int size) {
        LocalDateTime startDate = DateUtil.getStartDate(period);
        LocalDateTime endDate = DateUtil.getCurrentDate();
        Pageable pageable = PageRequest.of(page, size);

        Gender enumGender = gender.equals("all") ? null : Gender.valueOf(gender);
        Mbti enumMbti = mbti.equals("all") ? null : Mbti.valueOf(mbti);

        return matchRepository.findByKeywordAndFilters(postCategoryTitle, keyword, startDate, endDate, type, enumGender, enumMbti, pageable);
    }

    // postId를 가지고 게시물을 조회해서 postDto을 리턴해주는 메소드
    @Transactional
    public PostDto getPostDtoByPostId(Long postId) {
        Post findPost = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        // viewCount 증가
        findPost.incrementViewCount();
        Post post = matchRepository.save(findPost);

        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .postCategory(post.getPostCategory())
                .nickname(post.getUser().getNickname())
                .viewCount(post.getViewCount())
                .matchCount(post.getMatchCount())
                .endDate(post.getEndDate())
                .chatRoomId(post.getChatRoom().getId())
                .commentCount(post.getComments().size())
                .comments(post.getComments())
                .likeCount(post.getLikes().size())
                .isNew(Duration.between(post.getCreatedAt(), LocalDateTime.now()).toDays() < 1)
                .createdAt(post.getCreatedAt())
                .user(post.getUser())
                .build();
    }

    public Post getPostByPostId(Long postId) {
        Optional<Post> post = matchRepository.findById(postId);
        return post.orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
        if (post.hasChatRoom()) {
            throw new IllegalStateException("연결된 채팅방이 있는 경우 포스트를 삭제할 수 없습니다.");
        }
        matchRepository.delete(post);
    }

    // 연결된 채팅방이 있는지 확인
    public boolean checkPostHasChatRoom(Long postId) {
        Post post = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
        return post.hasChatRoom();
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