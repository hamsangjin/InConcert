package com.inconcert.domain.like.controller;

import com.inconcert.common.exception.CategoryNotFoundException;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.PostNotFoundException;
import com.inconcert.common.exception.UserNotFoundException;
import com.inconcert.domain.like.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LikeRestControllerTest {
    @Mock
    private LikeService likeService;

    @InjectMocks
    private LikeRestController likeRestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 좋아요_토글_성공_컨트롤러_테스트() {
        // Given
        Map<String, Boolean> responseMap = Map.of("liked", true);
        when(likeService.toggleLike(anyLong(), anyString())).thenReturn(ResponseEntity.ok(responseMap));

        // When
        ResponseEntity<Map<String, Boolean>> response = likeRestController.toggleLike(1L, "info");

        // Then
        verify(likeService, times(1)).toggleLike(1L, "info");
        assertEquals(responseMap, response.getBody());
    }

    @Test
    void 좋아요_상태_확인_성공_컨트롤러_테스트() {
        // Given
        Map<String, Boolean> responseMap = Map.of("liked", true);
        when(likeService.isLikedByUser(anyLong(), anyString())).thenReturn(ResponseEntity.ok(responseMap));

        // When
        ResponseEntity<Map<String, Boolean>> response = likeRestController.getLikeStatus(1L, "info");

        // Then
        verify(likeService, times(1)).isLikedByUser(1L, "info");
        assertEquals(responseMap, response.getBody());
    }

    @Test
    void 좋아요_토글_실패_포스트_찾을_수_없음_컨트롤러_테스트() {
        // Given
        when(likeService.toggleLike(anyLong(), anyString())).thenThrow(new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        // When
        PostNotFoundException exception = assertThrows(PostNotFoundException.class, () -> {
            likeRestController.toggleLike(1L, "info");
        });

        // Then
        verify(likeService, times(1)).toggleLike(1L, "info");
        assertEquals("찾으려는 게시글이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 좋아요_토글_실패_사용자_찾을_수_없음_컨트롤러_테스트() {
        // Given
        when(likeService.toggleLike(anyLong(), anyString())).thenThrow(new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // When
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            likeRestController.toggleLike(1L, "info");
        });

        // Then
        verify(likeService, times(1)).toggleLike(1L, "info");
        assertEquals("찾으려는 유저가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 좋아요_토글_실패_잘못된_카테고리_컨트롤러_테스트() {
        // Given
        when(likeService.toggleLike(anyLong(), anyString())).thenThrow(new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage()));

        // When
        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class, () -> {
            likeRestController.toggleLike(1L, "invalidCategory");
        });

        // Then
        verify(likeService, times(1)).toggleLike(1L, "invalidCategory");
        assertEquals("찾으려는 카테고리가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 좋아요_상태_확인_실패_포스트_찾을_수_없음_컨트롤러_테스트() {
        // Given
        when(likeService.isLikedByUser(anyLong(), anyString())).thenThrow(new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        // When
        PostNotFoundException exception = assertThrows(PostNotFoundException.class, () -> {
            likeRestController.getLikeStatus(1L, "info");
        });

        // Then
        verify(likeService, times(1)).isLikedByUser(1L, "info");
        assertEquals("찾으려는 게시글이 존재하지 않습니다.", exception.getMessage());
    }
}