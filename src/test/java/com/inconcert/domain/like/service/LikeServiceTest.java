package com.inconcert.domain.like.service;

import com.inconcert.common.exception.PostNotFoundException;
import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.like.repository.LikeRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private InfoRepository infoRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private LikeService likeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 좋아요_토글_성공() {
        // Given
        Long postId = 1L;
        String categoryTitle = "info";
        Post post = mock(Post.class);
        User user = mock(User.class);

        // Post 객체에서 getUser() 호출 시 User 객체 반환 설정
        when(post.getUser()).thenReturn(user);

        // Post의 ID와 User의 ID를 반환하도록 설정 (Mocking)
        when(user.getId()).thenReturn(1L);

        when(infoRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userService.getAuthenticatedUser()).thenReturn(Optional.of(user));
        when(likeRepository.findByPostAndUser(post, user)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Map<String, Boolean>> response = likeService.toggleLike(postId, categoryTitle);

        // Then
        verify(likeRepository, times(1)).save(any(Like.class));
        assertTrue(response.getBody().get("liked"));
    }

    @Test
    void 좋아요_취소_성공() {
        // Given
        Long postId = 1L;
        String categoryTitle = "info";
        Post post = mock(Post.class);
        User user = mock(User.class);
        Like like = mock(Like.class);

        // Post 객체에서 getUser() 호출 시 User 객체 반환 설정
        when(post.getUser()).thenReturn(user);

        // User의 ID를 반환하도록 설정 (Mocking)
        when(user.getId()).thenReturn(1L);

        when(infoRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userService.getAuthenticatedUser()).thenReturn(Optional.of(user));
        when(likeRepository.findByPostAndUser(post, user)).thenReturn(Optional.of(like));

        // When
        ResponseEntity<Map<String, Boolean>> response = likeService.toggleLike(postId, categoryTitle);

        // Then
        verify(likeRepository, times(1)).delete(like);
        assertTrue(response.getBody().get("liked"));
    }

    @Test
    void 로그인되지_않은_사용자의_좋아요_토글_실패() {
        // Given
        Long postId = 1L;
        String categoryTitle = "info";

        when(infoRepository.findById(postId)).thenReturn(Optional.of(mock(Post.class)));
        when(userService.getAuthenticatedUser()).thenReturn(Optional.empty());

        // When
        ResponseEntity<Map<String, Boolean>> response = likeService.toggleLike(postId, categoryTitle);

        // Then
        assertFalse(response.getBody().get("liked"));
    }

    @Test
    void 존재하지_않는_포스트_좋아요_토글_실패() {
        // Given
        Long postId = 1L;
        String categoryTitle = "info";

        when(infoRepository.findById(postId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(PostNotFoundException.class, () -> likeService.toggleLike(postId, categoryTitle));
    }

    @Test
    void 좋아요_상태_확인_성공() {
        // Given
        Long postId = 1L;
        String categoryTitle = "info";
        Post post = mock(Post.class);
        User user = mock(User.class);

        when(infoRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userService.getAuthenticatedUser()).thenReturn(Optional.of(user));
        when(likeRepository.existsByPostAndUser(post, user)).thenReturn(true);

        // When
        ResponseEntity<Map<String, Boolean>> response = likeService.isLikedByUser(postId, categoryTitle);

        // Then
        assertTrue(response.getBody().get("liked"));
    }
}