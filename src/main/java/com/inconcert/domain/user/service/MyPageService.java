package com.inconcert.domain.user.service;

import com.inconcert.domain.chat.repository.ChatRoomRepository;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.service.ImageService;
import com.inconcert.domain.feedback.repository.FeedbackRepository;
import com.inconcert.domain.user.dto.request.MyPageEditReqDto;
import com.inconcert.domain.user.dto.response.MatchRspDTO;
import com.inconcert.domain.user.dto.response.FeedbackRspDTO;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.MyPageRepostory;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.PostNotFoundException;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final MyPageRepostory myPageRepostory;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;
    private final MatchRepository matchRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional(readOnly = true)
    public Page<PostDTO> getMyPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return myPageRepostory.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getMyCommentPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return myPageRepostory.findPostsWithMyComments(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getMyLikePosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return myPageRepostory.findPostsILiked(userId, pageable);
    }

    // 유저 정보 수정
    @Transactional
    public void editUser(MyPageEditReqDto reqDto) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(reqDto.getPassword());

        // 이미지 처리
        String profileImageUrl = user.getProfileImage();
        if (reqDto.getProfileImage() != null && !reqDto.getProfileImage().isEmpty()) {
            Map<String, String> uploadResult = imageService.uploadImage(reqDto.getProfileImage());
            profileImageUrl = uploadResult.get("url");
        }

        user.updateUser(reqDto, encodedPassword, profileImageUrl);
        userRepository.save(user);
    }

    // 동행중
    public Page<MatchRspDTO> presentMatch(Long userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        return chatRoomRepository.findAllByUserId(userId, pageable);
    }

    // 동행 완료
    public Page<MatchRspDTO> completeMatch(Long userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        return matchRepository.findAllByUserIdANDEndMatch(userId, pageable);
    }


    public List<FeedbackRspDTO> getMyReviewee(Long userId, Long postId){
        Post post = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        List<FeedbackRspDTO> feedbackRspDTOs = userRepository.getFeedbackRspDTOByMatchUserIds(userId, post.getMatchUserIds());

        feedbackRspDTOs.forEach(dto -> {
            dto.setReviewerId(userId);
            dto.setPostId(postId);
        });

        return feedbackRspDTOs;
    }

    public List<Boolean> getUsersReviewStatuses(Long userId, Long postId){
        Post post = matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        List<Long> matchUserIds = post.getMatchUserIds().stream()
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());

        List<Long> revieweeIds = feedbackRepository.findExistingFeedbacks(userId, matchUserIds, postId);

        // admin이랑 user1을 돌아
        return matchUserIds.stream()
                // adminId가 이미 리뷰완료 Id에 포함돼 ?
                .map(id -> revieweeIds.contains(id))
                .collect(Collectors.toList());
    }

    public List<Boolean> isEndFeedback(Long userId, List<Long> postIds){
        List<Boolean> result = new ArrayList<>();
        for(Long postId : postIds){
            List<Boolean> usersReviewStatuses = getUsersReviewStatuses(userId, postId);

            boolean flag = true;
            for (Boolean b : usersReviewStatuses) {
                if (!b){
                    flag = false;
                    break;
                }
            }
            result.add(flag);
        }

        return result;
    }
}