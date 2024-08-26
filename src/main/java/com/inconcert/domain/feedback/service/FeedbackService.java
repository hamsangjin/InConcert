package com.inconcert.domain.feedback.service;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.feedback.entity.Feedback;
import com.inconcert.domain.feedback.repository.FeedbackRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.PostNotFoundException;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public void feedback(Long postId, Long reviewerId, Long revieweeId, int point){
        Post post = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        User reviewee = userRepository.findById(revieweeId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        Feedback feedback = Feedback.builder()
                .point(point)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .post(post)
                .build();

        // 피드백 저장
        feedbackRepository.save(feedback);

        // 피드백에서 reviewee의 평균점수를 매겨서 reviewee의 mannerPoint에 반영
        userRepository.updateMannerPointByRevieweeId(revieweeId);
    }
}
