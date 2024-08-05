package com.inconcert.domain.like.service;

import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.like.repository.LikeRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.*;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.CategoryNotFoundException;
import com.inconcert.global.exception.PostNotFoundException;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    public boolean toggleLike(Long postId, String categoryTitle) {
        Post post = getPost(postId, categoryTitle);

        User user = getUser();
        if (user == null) return false;

        // 현재 사용자가 해당 포스트에 좋아요를 눌렀는지 확인
        Optional<Like> findLike = likeRepository.findByPostAndUser(post, user);

        if (!findLike.isPresent()) {
            // 좋아요를 누르지 않은 경우, 좋아요 추가
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
        } else {
            // 이미 좋아요를 누른 경우, 좋아요 취소
            likeRepository.delete(findLike.get());
        }
        return true;
    }

    public boolean isLikedByUser(Long postId, String categoryTitle) {
        Post post = getPost(postId, categoryTitle);

        User user = getUser();
        if (user == null) return false;

        return likeRepository.existsByPostAndUser(post, user);
    }

    // 카테고리 제목에 맞는 repository에서 post 찾는 메소드
    private Post getPost(Long postId, String categoryTitle) {
        Post post = switch (categoryTitle) {
            case "info" -> infoRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
            case "match" -> matchRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
            case "review" -> reviewRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
            case "transfer" -> transferRepository.findById(postId)
                    .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
            default -> throw new CategoryNotFoundException("카테고리를 찾을 수 없습니다.");
        };
        return post;
    }

    private User getUser() {
        Optional<User> optionalUser = userService.getAuthenticatedUser();
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
        return user;
    }
}