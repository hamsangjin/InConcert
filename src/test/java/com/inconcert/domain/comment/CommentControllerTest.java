package com.inconcert.domain.comment;

import com.inconcert.domain.comment.controller.CommentController;
import com.inconcert.domain.comment.dto.CommentCreationDTO;
import com.inconcert.domain.comment.service.CommentService;
import com.inconcert.global.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CommentControllerTest {

    // CommentService를 Mocking하여 실제 구현체가 아닌 가짜 객체로 동작하게 함
    @Mock
    private CommentService infoCommentService;

    // CommentController를 테스트하기 위해 InjectMocks를 사용하여
    // Mock된 CommentService를 주입받은 CommentController 객체를 생성
    @InjectMocks
    private CommentController commentController;

    // 각 테스트 실행 전에 Mock 객체를 초기화합니다.
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 댓글_생성_테스트() {
        // Given
        // 댓글 생성용 DTO 객체를 생성하고, 테스트용 댓글 내용을 설정
        CommentCreationDTO commentCreationDTO = new CommentCreationDTO();
        commentCreationDTO.setContent("Test comment");

        // BindingResult와 RedirectAttributes를 Mocking
        BindingResult bindingResult = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        when(bindingResult.hasErrors()).thenReturn(false);  // Validation 오류가 없도록 설정

        // Service 계층의 saveComment 메소드가 호출될 때 아무 동작도 하지 않도록 설정
        doNothing().when(infoCommentService).saveComment(anyString(), anyLong(), any(CommentCreationDTO.class));

        // When
        // CommentController의 createComment 메소드를 호출하여 댓글을 생성
        String result = commentController.createComment("info", "musical", 1L, commentCreationDTO, bindingResult, redirectAttributes);

        // Then
        // Service 계층의 saveComment 메소드가 정확히 한 번 호출되었는지 검증
        verify(infoCommentService, times(1)).saveComment(anyString(), anyLong(), any(CommentCreationDTO.class));
        // 생성 후 페이지가 올바르게 리다이렉트 되었는지 검증
        assertEquals("redirect:/info/musical/1", result);
    }

    @Test
    void 대댓글_생성_테스트() {
        // Given
        // 대댓글 생성용 DTO 객체를 생성하고, 테스트용 대댓글 내용을 설정
        CommentCreationDTO commentCreationDTO = new CommentCreationDTO();
        commentCreationDTO.setContent("Test reply");

        // BindingResult와 RedirectAttributes를 Mocking
        BindingResult bindingResult = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        when(bindingResult.hasErrors()).thenReturn(false);  // Validation 오류가 없도록 설정

        // Service 계층의 saveReply 메소드가 호출될 때 아무 동작도 하지 않도록 설정
        doNothing().when(infoCommentService).saveReply(anyString(), anyLong(), anyLong(), any(CommentCreationDTO.class));

        // When
        // CommentController의 createReply 메소드를 호출하여 대댓글을 생성
        String result = commentController.createReply("info", "musical", 1L, 2L, commentCreationDTO, bindingResult, redirectAttributes);

        // Then
        // Service 계층의 saveReply 메소드가 정확히 한 번 호출되었는지 검증
        verify(infoCommentService, times(1)).saveReply(anyString(), anyLong(), anyLong(), any(CommentCreationDTO.class));
        // 생성 후 페이지가 올바르게 리다이렉트 되었는지 검증
        assertEquals("redirect:/info/musical/1", result);
    }

    @Test
    void 댓글_삭제_테스트() {
        // Given
        // 삭제할 댓글의 ID를 설정
        Long commentId = 1L;

        // Service 계층의 deleteComment 메소드가 호출될 때 아무 동작도 하지 않도록 설정
        doNothing().when(infoCommentService).deleteComment(anyString(), anyLong());

        // When
        // CommentController의 deleteComment 메소드를 호출하여 댓글을 삭제
        String result = commentController.deleteComment("info", "musical", 1L, commentId);

        // Then
        // Service 계층의 deleteComment 메소드가 정확히 한 번 호출되었는지 검증
        verify(infoCommentService, times(1)).deleteComment(anyString(), anyLong());
        // 삭제 후 페이지가 올바르게 리다이렉트 되었는지 검증
        assertEquals("redirect:/info/musical/1", result);
    }

    @Test
    void 댓글_수정_테스트() {
        // Given
        // 수정할 댓글의 ID와 수정 내용을 설정
        Long commentId = 1L;
        CommentCreationDTO commentForm = new CommentCreationDTO();
        commentForm.setContent("Updated comment");

        // BindingResult를 Mocking하여 Validation 오류가 없도록 설정
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        // Service 계층의 updateComment 메소드가 호출될 때 아무 동작도 하지 않도록 설정
        doNothing().when(infoCommentService).updateComment(anyString(), eq(commentId), any(CommentCreationDTO.class));

        // When
        // CommentController의 editComment 메소드를 호출하여 댓글을 수정
        String result = commentController.editComment("info", "musical", 1L, commentId, commentForm, bindingResult);

        // Then
        // Service 계층의 updateComment 메소드가 정확히 한 번 호출되었는지 검증
        verify(infoCommentService, times(1)).updateComment(anyString(), eq(commentId), any(CommentCreationDTO.class));
        // 수정 후 페이지가 올바르게 리다이렉트 되었는지 검증
        assertEquals("redirect:/info/musical/1", result);
    }

    @Test
    void 비어있는_댓글_생성_테스트() {
        // Given
        // 내용이 빈 댓글 생성용 DTO 객체를 생성
        CommentCreationDTO commentCreationDTO = new CommentCreationDTO();
        commentCreationDTO.setContent("");

        // BindingResult를 Mocking하여 Validation 오류가 발생하도록 설정
        BindingResult bindingResult = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        // CommentController의 createComment 메소드를 호출하여 빈 댓글 생성 시도
        String result = commentController.createComment("info", "musical", 1L, commentCreationDTO, bindingResult, redirectAttributes);

        // Then
        // Service 계층의 saveComment 메소드가 호출되지 않았는지 검증
        verify(infoCommentService, never()).saveComment(anyString(), anyLong(), any(CommentCreationDTO.class));
        // Validation 오류가 발생했을 때 페이지가 올바르게 리다이렉트 되었는지 검증
        assertEquals("redirect:/info/musical/1", result);
    }

    @Test
    void 존재하지_않는_댓글_수정_테스트() {
        // Given
        // 존재하지 않는 댓글 ID와 수정 내용을 설정
        Long nonExistentCommentId = 999L;
        CommentCreationDTO commentForm = new CommentCreationDTO();
        commentForm.setContent("Updated comment");

        // BindingResult를 Mocking하여 Validation 오류가 없도록 설정
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        // Service 계층에서 댓글을 찾지 못하면 예외를 발생시키도록 설정
        doThrow(new IllegalArgumentException("Comment not found"))
                .when(infoCommentService).updateComment(anyString(), eq(nonExistentCommentId), any(CommentCreationDTO.class));

        // When / Then
        // CommentController의 editComment 메소드를 호출하여 예외가 발생하는지 검증
        assertThrows(IllegalArgumentException.class, () -> {
            commentController.editComment("info", "musical", 1L, nonExistentCommentId, commentForm, bindingResult);
        });
    }

    @Test
    void 인증되지_않은_사용자의_댓글_생성_테스트() {
        // Given
        // 댓글 생성용 DTO 객체를 생성하고, 테스트용 댓글 내용을 설정
        CommentCreationDTO commentCreationDTO = new CommentCreationDTO();
        commentCreationDTO.setContent("Test comment");

        // BindingResult와 RedirectAttributes를 Mocking
        BindingResult bindingResult = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        when(bindingResult.hasErrors()).thenReturn(false);  // Validation 오류가 없도록 설정

        // Service 계층에서 인증되지 않은 사용자가 댓글을 생성하려고 하면 UserNotFoundException을 발생시키도록 설정
        doThrow(new UserNotFoundException("로그인 후 이용 가능합니다."))
                .when(infoCommentService).saveComment(anyString(), anyLong(), any(CommentCreationDTO.class));

        // When
        // CommentController의 createComment 메소드를 호출하여 댓글을 생성 시도
        String result = commentController.createComment("info", "musical", 1L, commentCreationDTO, bindingResult, redirectAttributes);

        // Then
        // Service 계층의 saveComment 메소드가 정확히 한 번 호출되었는지 검증
        verify(infoCommentService, times(1)).saveComment(anyString(), anyLong(), any(CommentCreationDTO.class));
        // RedirectAttributes 객체가 "로그인 후 이용 가능합니다." 메시지를 포함하고 있는지 검증
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("message"), eq("로그인 후 이용 가능합니다."));
        // 인증되지 않은 사용자의 경우 로그인 페이지로 리다이렉트 되었는지 검증
        assertEquals("redirect:/loginform", result);
    }

    @Test
    void 인증되지_않은_사용자의_대댓글_생성_테스트() {
        // Given
        // 대댓글 생성용 DTO 객체를 생성하고, 테스트용 대댓글 내용을 설정
        CommentCreationDTO commentCreationDTO = new CommentCreationDTO();
        commentCreationDTO.setContent("Test reply");

        // BindingResult와 RedirectAttributes를 Mocking
        BindingResult bindingResult = mock(BindingResult.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        when(bindingResult.hasErrors()).thenReturn(false);  // Validation 오류가 없도록 설정

        // Service 계층에서 인증되지 않은 사용자가 대댓글을 생성하려고 하면 UserNotFoundException을 발생시키도록 설정
        doThrow(new UserNotFoundException("로그인 후 이용 가능합니다."))
                .when(infoCommentService).saveReply(anyString(), anyLong(), anyLong(), any(CommentCreationDTO.class));

        // When
        // CommentController의 createReply 메소드를 호출하여 대댓글 생성 시도
        String result = commentController.createReply("info", "musical", 1L, 2L, commentCreationDTO, bindingResult, redirectAttributes);

        // Then
        // Service 계층의 saveReply 메소드가 정확히 한 번 호출되었는지 검증
        verify(infoCommentService, times(1)).saveReply(anyString(), anyLong(), anyLong(), any(CommentCreationDTO.class));
        // RedirectAttributes 객체가 "로그인 후 이용 가능합니다." 메시지를 포함하고 있는지 검증
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("message"), eq("로그인 후 이용 가능합니다."));
        // 인증되지 않은 사용자의 경우 로그인 페이지로 리다이렉트 되었는지 검증
        assertEquals("redirect:/loginform", result);
    }
}
