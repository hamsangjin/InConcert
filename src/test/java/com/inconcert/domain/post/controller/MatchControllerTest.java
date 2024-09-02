package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MatchControllerTest {

    @Mock
    private MatchService matchService;

    // 테스트할 MatchController에 Mock된 서비스들을 주입
    @InjectMocks
    private MatchController matchController;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    // 각 테스트가 실행되기 전에 Mock 객체들을 초기화
    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 채팅방이_연결된_게시글_삭제_테스트() {
        // Given
        Long postId = 1L;
        String postCategoryTitle = "concert";

        // 채팅방이 연결된 게시글을 삭제할 때 예외를 발생시키도록 설정
        doThrow(new IllegalStateException("Cannot delete post with connected chat room"))
                .when(matchService).deletePost(postId);

        // When
        // MatchController의 deletePost 메소드를 호출하여 삭제 시도
        String result = matchController.deletePost(postCategoryTitle, postId, redirectAttributes);

        // Then
        // 서비스 계층의 deletePost 메소드가 정확히 한 번 호출되었는지 검증
        verify(matchService, times(1)).deletePost(postId);
        // RedirectAttributes 객체에 오류 메시지가 추가되었는지 검증
        verify(redirectAttributes, times(1))
                .addFlashAttribute("errorMessage", "Cannot delete post with connected chat room");
        // 리다이렉트 결과가 예상과 일치하는지 검증
        assertEquals("redirect:/match/concert/1", result);
    }

    @Test
    void 채팅방이_연결된_게시글_수정_폼_테스트() {
        // Given
        Long postId = 1L;
        String postCategoryTitle = "concert";
        PostDTO postDTO = new PostDTO();

        // 게시글 DTO 반환 및 채팅방이 연결된 상태로 설정
        when(matchService.getPostDtoByPostId(postId)).thenReturn(postDTO);
        when(matchService.checkPostHasChatRoom(postId)).thenReturn(true);

        // When
        // MatchController의 editPostForm 메소드를 호출하여 수정 폼 조회
        String result = matchController.editPostForm(postCategoryTitle, postId, model);

        // Then
        // 서비스 계층 메소드 호출 및 모델에 데이터 추가 검증
        verify(matchService, times(1)).getPostDtoByPostId(postId);
        verify(matchService, times(1)).checkPostHasChatRoom(postId);
        verify(model, times(1)).addAttribute("post", postDTO);
        verify(model, times(1)).addAttribute("hasChatRoom", "true");

        // 반환된 뷰 이름이 예상과 일치하는지 검증
        assertEquals("board/editform", result);
    }

    @Test
    void 매칭_완료_테스트() {
        // Given
        Long postId = 1L;
        String postCategoryTitle = "concert";

        // When
        // MatchController의 completeMatch 메소드를 호출하여 매칭 완료 처리
        String result = matchController.completeMatch(postId, postCategoryTitle);

        // Then
        // 서비스 계층의 completeMatch 메소드가 정확히 한 번 호출되었는지 검증
        verify(matchService, times(1)).completeMatch(postId);
        // 리다이렉트 결과가 예상과 일치하는지 검증
        assertEquals("redirect:/match/concert/1", result);
    }
}