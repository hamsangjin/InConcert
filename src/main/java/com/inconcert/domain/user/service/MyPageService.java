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
import com.inconcert.domain.user.repository.MyPageRepository;
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
    private final MyPageRepository myPageRepository;
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

        return myPageRepository.getPostDTOsByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getMyCommentPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return myPageRepository.getPostDTOsWithMyComments(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getMyLikePosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return myPageRepository.getPostDTOsMyLiked(userId, pageable);
    }

    // 유저 정보 수정
    @Transactional
    public void editUser(MyPageEditReqDto reqDto) {
        User user = getAuthenticatedUser();

        // 비밀번호 인코딩(중복 인코딩 제외 처리)
        String encodedPassword = reqDto.getPassword().startsWith("$2a$10") ? reqDto.getPassword() : passwordEncoder.encode(reqDto.getPassword());

        // 이미지 처리
        String profileImageUrl = user.getProfileImage();
        // 이미지 변경 여부 확인
        if (reqDto.getProfileImage() != null && !reqDto.getProfileImage().isEmpty()) {
            // 기존 이미지가 기본 이미지가 아니면 s3 삭제
            if(!profileImageUrl.equals("/images/profile.png")) {
                imageService.deleteImage(imageService.extractImageKeyFromUrl(profileImageUrl));
            }
            // 새로운 프로필 이미지 업로드
            Map<String, String> map = (Map<String, String>) imageService.uploadImage(reqDto.getProfileImage()).getBody();
            profileImageUrl = map.get("url");
        }
        user.updateUser(reqDto, encodedPassword, profileImageUrl);
        userRepository.save(user);
    }

    // 기본 이미지로 변경
    @Transactional
    public ResponseEntity<String> resetToDefaultProfileImage() {
        User user = getAuthenticatedUser();

        // 기존 이미지가 기본 이미지가 아니면 s3 삭제
        if(!user.getProfileImage().equals("/images/profile.png")) {
            imageService.deleteImage(imageService.extractImageKeyFromUrl(user.getProfileImage()));
        }

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
        List<Long> revieweeIds = feedbackRepository.getExistingRevieweeIdsByReviewerAndPost(userId, matchUserIds, postId);

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

    private User getAuthenticatedUser(){
        return userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
    }
}