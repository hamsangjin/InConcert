package com.inconcert.domain.post.service;

import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.chat.repository.ChatRoomRepository;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.util.DateUtil;
import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import com.inconcert.domain.user.entity.User;
import com.inconcert.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {
    private final MatchRepository matchRepository;
    private final ChatRoomRepository chatRoomRepository;

    public List<PostDTO> getAllMatchPostsByPostCategory(String postCategoryTitle) {
        return switch (postCategoryTitle) {
            case "musical" -> matchRepository.findPostsByPostCategoryTitle("musical");
            case "concert" -> matchRepository.findPostsByPostCategoryTitle("concert");
            case "theater" -> matchRepository.findPostsByPostCategoryTitle("theater");
            case "etc" -> matchRepository.findPostsByPostCategoryTitle("etc");
            default -> throw new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_NOT_FOUND.getMessage());
        };
    }

    public Page<PostDTO> getAllInfoPostsByPostCategory(String postCategoryTitle, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return matchRepository.findPostsByPostCategoryTitle(postCategoryTitle, pageable);
    }

    public Page<PostDTO> getByKeywordAndFilters(String postCategoryTitle, String keyword, String period, String type, String gender, String mbti, int page, int size) {
        LocalDateTime startDate = DateUtil.getStartDate(period);
        LocalDateTime endDate = DateUtil.getCurrentDate();
        Pageable pageable = PageRequest.of(page, size);

        Gender enumGender = gender.equals("all") ? null : Gender.valueOf(gender);
        Mbti enumMbti = mbti.equals("all") ? null : Mbti.valueOf(mbti);

        return matchRepository.findByKeywordAndFilters(postCategoryTitle, keyword, startDate, endDate, type, enumGender, enumMbti, pageable);
    }

    // postId를 가지고 게시물을 조회해서 postDto을 리턴해주는 메소드
    @Transactional
    public PostDTO getPostDtoByPostId(Long postId) {
        Post findPost = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        // viewCount 증가
        findPost.incrementViewCount();
        Post post = matchRepository.save(findPost);

        return PostDTO.builder()
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
                .chatRoomUserSize(post.getChatRoom().getUsers().size())
                .isEnd(post.isEnd())
                .build();
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
        if (post.hasChatRoom() && post.getChatRoom().getUsers().size() >= 2) {
            throw new ExistChatPostDeleteException(ExceptionMessage.EXIST_CHAT_POST_DELETE.getMessage());
        }
        matchRepository.delete(post);
    }

    // 연결된 채팅방이 있는지 확인
    public boolean checkPostHasChatRoom(Long postId) {
        Post post = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
        return post.hasChatRoom();
    }

    @Transactional
    public void completeMatch(Long postId){
        Post post = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId);

        List<Long> matchUserIds = chatRoom.getUsers()
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());
        post.updateMatchUserIds(matchUserIds);
        post.toggleIsEnd();
        matchRepository.save(post);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
    public void updatePostStatus() {
        List<Post> posts = matchRepository.findAllByEndDateBeforeAndIsEndFalse(LocalDate.now());
        for (Post post : posts) {
            ChatRoom chatRoom = chatRoomRepository.findByPostId(post.getId());
            List<Long> matchUserIds = chatRoom.getUsers()
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            post.updateMatchUserIds(matchUserIds);
            post.toggleIsEnd();
            matchRepository.save(post);
        }
    }
}