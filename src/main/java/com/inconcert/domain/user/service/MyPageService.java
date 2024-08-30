package com.inconcert.domain.user.service;

import com.inconcert.domain.chat.repository.ChatRoomRepository;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.common.service.ImageService;
import com.inconcert.domain.feedback.repository.FeedbackRepository;
import com.inconcert.domain.user.dto.request.MyPageEditReqDto;
import com.inconcert.domain.user.dto.response.MatchRspDTO;
import com.inconcert.domain.user.dto.response.FeedbackRspDTO;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.MyPageRepostory;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return myPageRepostory.getPostDTOsByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getMyCommentPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return myPageRepostory.getPostDTOsWithMyComments(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getMyLikePosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return myPageRepostory.getPostDTOsMyLiked(userId, pageable);
    }

    // 유저 정보 수정
    @Transactional
    public void editUser(MyPageEditReqDto reqDto) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(reqDto.getPassword());

        // 이미지 처리(기존 이미지 삭제 처리 추가 필요)
        String profileImageUrl = user.getProfileImage();
        if (reqDto.getProfileImage() != null && !reqDto.getProfileImage().isEmpty()) {
            Map<String, String> map = (Map<String, String>) imageService.uploadImage(reqDto.getProfileImage()).getBody();
            profileImageUrl = map.get("url");
        }

        user.updateUser(reqDto, encodedPassword, profileImageUrl);
        userRepository.save(user);
    }

    // 기본 이미지로 변경(기존 이미지 삭제 처리 추가 필요)
    @Transactional
    public ResponseEntity<String> resetToDefaultProfileImage() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        user.setBasicImage();
        userRepository.save(user);

        return ResponseEntity.ok("이미지가 정상적으로 변경되었습니다.");
    }

    // 동행중
    public Page<MatchRspDTO> presentMatch(Long userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        return chatRoomRepository.getChatRoomDTOsByUserId(userId, pageable);
    }

    // 동행 완료
    public Page<MatchRspDTO> completeMatch(Long userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        return matchRepository.getMatchRspDTOsByUserIdInMatchUserIdsAndEndMatch(userId, pageable);
    }

    // 해당 게시글의 평가할 유저들 불러오기
    public List<FeedbackRspDTO> getMyReviewee(Long userId, Long postId){
        // 본인을 제외한 리뷰 대상 유저 불러오기
        List<Long> matchUserIds = matchRepository.findMatchUsersByPostId(postId, userId);

        // 리뷰 유저 대상들의 정보들 DTO에 담아서 반환
        return userRepository.getFeedbackRspDTOByMatchUserIds(userId, postId, matchUserIds);
    }

    public List<Boolean> getUsersReviewStatuses(Long userId, Long postId){
        // 본인을 제외한 리뷰 대상 유저 불러오기
        List<Long> matchUserIds = matchRepository.findMatchUsersByPostId(postId, userId);

        // 이미 본인이 리뷰를 남긴 유저의 id들 불러오기
        List<Long> revieweeIds = feedbackRepository.findExistingFeedbacks(userId, matchUserIds, postId);

        // matchUserIds를 각각 돌면서 revieweeIds에 포함되었는지 확인
        return matchUserIds.stream()
                .map(revieweeIds::contains)
                .collect(Collectors.toList());
    }

    public List<Boolean> isEndFeedback(Long userId, List<Long> postIds){
        // postIds를 각각 돌면서 getUsersReviewStatuses메소드를 호출해 전부 true인지 확인한 결과(리뷰 끝 여부)를 반환
        return postIds.stream()
                .map(postId -> getUsersReviewStatuses(userId, postId)
                        .stream()
                        .allMatch(Boolean::booleanValue))
                .collect(Collectors.toList());
    }
}