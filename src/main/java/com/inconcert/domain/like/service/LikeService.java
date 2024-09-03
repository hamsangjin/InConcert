package com.inconcert.domain.like.service;

import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.like.repository.LikeRepository;
import com.inconcert.domain.notification.service.NotificationService;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.*;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.common.exception.CategoryNotFoundException;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.PostNotFoundException;
import com.inconcert.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final InfoRepository infoRepository;
    private final MatchRepository matchRepository;
    private final ReviewRepository reviewRepository;
    private final TransferRepository transferRepository;
    private final LikeRepository likeRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public ResponseEntity<Map<String, Boolean>> toggleLike(Long postId, String categoryTitle) {
        Post post = getPost(postId, categoryTitle);
        User user = getAuthenticatedUser();
        Map<String, Boolean> response = new HashMap<>();

        // 로그인하지 않은 경우
        if (user == null){
            response.put("liked", false);
            return ResponseEntity.ok(response);
        }

        // 현재 사용자가 해당 포스트에 좋아요를 눌렀는지 확인
        Optional<Like> findLike = likeRepository.findByPostAndUser(post, user);

        // 좋아요를 누르지 않은 경우, 좋아요 추가
        if (findLike.isEmpty()) {
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            if(!post.getUser().getId().equals(user.getId()))
                notificationService.createLikesNotification(post, user, true);

            likeRepository.save(like);
        }
        // 이미 좋아요를 누른 경우, 좋아요 취소
        else {
            if(!post.getUser().getId().equals(user.getId()))
                notificationService.createLikesNotification(post, user, false);

            likeRepository.delete(findLike.get());
        }

        response.put("liked", true);
        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> isLikedByUser(Long postId, String categoryTitle) {
        Post post = getPost(postId, categoryTitle);
        Map<String, Boolean> response = new HashMap<>();

        User user = getAuthenticatedUser();
        if (user == null){
            response.put("liked", false);
            return ResponseEntity.ok(response);
        }

        response.put("liked", likeRepository.existsByPostAndUser(post, user));
        return ResponseEntity.ok(response);
    }

    // 카테고리 제목에 맞는 repository에서 post 찾는 메소드
    private Post getPost(Long postId, String categoryTitle) {
        return switch (categoryTitle) {
            case "info" -> infoRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.LIKE_NOT_FOUND.getMessage()));
            case "match" -> matchRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.LIKE_NOT_FOUND.getMessage()));
            case "review" -> reviewRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.LIKE_NOT_FOUND.getMessage()));
            case "transfer" -> transferRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.LIKE_NOT_FOUND.getMessage()));
            default -> throw new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage());
        };
    }

    private User getAuthenticatedUser() {
        Optional<User> optionalUser = userService.getAuthenticatedUser();
        if (optionalUser.isEmpty()) return null;

        return optionalUser
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
    }
}