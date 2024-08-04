package com.inconcert.domain.like.service;

import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.like.repository.LikeRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.*;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.LikeNotFoundException;
import com.inconcert.global.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public boolean toggleLike(Long postId,String categoryTitle) {
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

        User user = userService.getAuthenticatedUser();


        // 현재 사용자가 해당 포스트에 좋아요를 눌렀는지 확인
        Like like = likeRepository.findByPostAndUser(post, user).orElseThrow(()->new LikeNotFoundException("좋아요 한 유저를"));

        if (like == null) {
            // 좋아요를 누르지 않은 경우, 좋아요 추가
            like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
            return true;
        } else {
            // 이미 좋아요를 누른 경우, 좋아요 취소
            likeRepository.delete(like);
            return false;
        }
    }






}
