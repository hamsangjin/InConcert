package com.inconcert.domain.user.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.service.ImageService;
import com.inconcert.domain.user.dto.request.MyPageEditReqDto;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.MyPageRepostory;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public List<PostDto> mypageBoard(Long userId) {
        List<Post> posts = myPageRepostory.findByUserId(userId);
        return getPostDtos(posts);
    }

    @Transactional(readOnly = true)
    public List<PostDto> mypageComment(Long userId) {
        List<Post> posts = myPageRepostory.findPostsWithMyComments(userId);
        return getPostDtos(posts);
    }

    @Transactional(readOnly = true)
    public List<PostDto> mypageLike(Long userId) {
        List<Post> posts = myPageRepostory.findPostsILiked(userId);
        return getPostDtos(posts);
    }

    private static List<PostDto> getPostDtos(List<Post> posts) {
        List<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {

            PostCategory postCategory = post.getPostCategory();
            Category category = postCategory.getCategory();

            PostDto postDto = PostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .category(category)
                    .postCategory(postCategory)
                    .thumbnailUrl(post.getThumbnailUrl())
                    .nickname(post.getUser().getNickname())
                    .viewCount(post.getViewCount())
                    .commentCount(post.getComments().size())
                    .likeCount(post.getLikes().size())
                    .isNew(Duration.between(post.getCreatedAt(), LocalDateTime.now()).toDays() < 1)
                    .createdAt(post.getCreatedAt())
                    .build();

            postDtos.add(postDto);
        }
        return postDtos;
    }

    // 유저 정보 수정
    @Transactional
    public User editUser(MyPageEditReqDto reqDto) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(reqDto.getPassword());

        // 이미지 처리
        String profileImageUrl = user.getProfileImage();
        if (reqDto.getProfileImage() != null && !reqDto.getProfileImage().isEmpty()) {
            Map<String, String> uploadResult = imageService.uploadImage(reqDto.getProfileImage());
            profileImageUrl = uploadResult.get("url");
        }

        user.updateUser(reqDto, encodedPassword, profileImageUrl);
        return userRepository.save(user);
    }
}
