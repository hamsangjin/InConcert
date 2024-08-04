package com.inconcert.domain.like.service;

import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.like.repository.LikeRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.*;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.LikeNotFoundException;
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
        Post post = null;

        switch (categoryTitle){
            case "info":
                post = infoRepository.findById(postId)
                        .orElseThrow(() -> new PostNotFoundException("게시글을 찾을수 없습니다."));
                break;
            case "match":
                post = matchRepository.findById(postId)
                        .orElseThrow(() -> new PostNotFoundException("게시글을 찾을수 없습니다."));
                break;
            case "review":
                post = reviewRepository.findById(postId)
                        .orElseThrow(() -> new PostNotFoundException("게시글을 찾을수 없습니다."));
                break;
            case "transfer":
                post = transferRepository.findById(postId)
                        .orElseThrow(() -> new PostNotFoundException("게시글을 찾을수 없습니다."));
                break;
        }

        Optional<User> loginUser = userService.getAuthenticatedUser();
        User user = null;
        if(!loginUser.isPresent())  return false;
        else                        user = loginUser.get();

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
        Post post = null;

        switch (categoryTitle){
            case "info":
                post = infoRepository.findById(postId).orElseThrow(()->new PostNotFoundException("게시글을 찾을수 없습니다."));
                break;
            case "match":
                post = matchRepository.findById(postId).orElseThrow(()->new PostNotFoundException("게시글을 찾을수 없습니다."));
                break;
            case "review":
                post = reviewRepository.findById(postId).orElseThrow(()->new PostNotFoundException("게시글을 찾을수 없습니다."));
                break;
            case "transfer":
                post = transferRepository.findById(postId).orElseThrow(()->new PostNotFoundException("게시글을 찾을수 없습니다."));
                break;
        }

        User user = userService.getAuthenticatedUser().orElseThrow(() -> new UserNotFoundException("user not found."));

        return likeRepository.existsByPostAndUser(post, user);
    }
}
