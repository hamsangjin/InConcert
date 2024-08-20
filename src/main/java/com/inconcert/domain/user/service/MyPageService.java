package com.inconcert.domain.user.service;

import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.service.ImageService;
import com.inconcert.domain.user.dto.request.MyPageEditReqDto;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.MyPageRepostory;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final MyPageRepostory myPageRepostory;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;

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
}