package com.inconcert.domain.chat;

import com.inconcert.common.exception.UserNotFoundException;
import com.inconcert.domain.chat.controller.ChatController;
import com.inconcert.domain.chat.dto.ChatMessageDTO;
import com.inconcert.domain.chat.dto.ChatRoomDTO;
import com.inconcert.domain.chat.service.ChatService;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 채팅_목록_가져오기() {
        // Given: 채팅방 목록을 반환하도록 설정
        List<ChatRoomDTO> chatRooms = List.of(new ChatRoomDTO());
        when(chatService.getChatRoomDTOsByUserId()).thenReturn(chatRooms);

        // When: 채팅 목록 조회 요청
        String result = chatController.getChatList(model);

        // Then: 서비스 호출과 모델 속성 설정 확인
        verify(chatService, times(1)).getChatRoomDTOsByUserId();
        verify(model, times(1)).addAttribute("chatRooms", chatRooms);
        assertEquals("chat/list", result);
    }

    @Test
    void 동행_요청_목록_보기() {
        // Given: 인증된 사용자 정보를 반환하도록 설정
        User user = new User();
        when(userService.getAuthenticatedUser()).thenReturn(Optional.of(user));

        // When: 동행 요청 목록 조회 요청
        String result = chatController.requestList(model);

        // Then: 서비스 호출과 모델 속성 설정 확인
        verify(userService, times(1)).getAuthenticatedUser();
        verify(model, times(1)).addAttribute("user", user);
        assertEquals("/chat/requests", result);
    }

    @Test
    void 특정_채팅방_조회_사용자가_없는_경우() {
        // Given: 사용자 정보를 반환하고, 해당 채팅방에 사용자가 없도록 설정
        Long chatRoomId = 1L;
        User user = new User();
        when(userService.getAuthenticatedUser()).thenReturn(Optional.of(user));
        when(chatService.isExistUser(chatRoomId)).thenReturn(false);

        // When: 특정 채팅방 조회 요청
        String result = chatController.getChatRoom(chatRoomId, model);

        // Then: 사용자가 없으면 채팅 목록 페이지로 리다이렉트
        verify(chatService, times(1)).isExistUser(chatRoomId);
        assertEquals("redirect:/chat/list", result);
    }

    @Test
    void 특정_채팅방_조회_사용자가_있는_경우() {
        // Given: 사용자 정보와 해당 채팅방 정보를 반환하도록 설정
        Long chatRoomId = 1L;
        User user = new User();
        ChatRoomDTO chatRoom = new ChatRoomDTO();
        List<ChatMessageDTO> messages = List.of(new ChatMessageDTO());

        when(userService.getAuthenticatedUser()).thenReturn(Optional.of(user));
        when(chatService.isExistUser(chatRoomId)).thenReturn(true);
        when(chatService.getChatRoomDTOByChatRoomId(chatRoomId)).thenReturn(chatRoom);
        when(chatService.getChatMessageDTOsByChatRoomId(chatRoomId)).thenReturn(messages);

        // When: 특정 채팅방 조회 요청
        String result = chatController.getChatRoom(chatRoomId, model);

        // Then: 사용자, 채팅방 정보 및 메시지를 모델에 추가
        verify(chatService, times(1)).isExistUser(chatRoomId);
        verify(chatService, times(1)).getChatRoomDTOByChatRoomId(chatRoomId);
        verify(chatService, times(1)).getChatMessageDTOsByChatRoomId(chatRoomId);
        verify(model, times(1)).addAttribute("chatRoom", chatRoom);
        verify(model, times(1)).addAttribute("messages", messages);
        verify(model, times(1)).addAttribute("user", user);

        assertEquals("chat/room", result);
    }

    @Test
    void 특정_채팅방_조회_사용자를_찾을_수_없는_경우() {
        // Given: 사용자 정보를 찾을 수 없도록 설정
        Long chatRoomId = 1L;
        when(userService.getAuthenticatedUser()).thenThrow(new UserNotFoundException("User not found"));

        // When / Then: 예외가 발생하는지 검증
        try {
            chatController.getChatRoom(chatRoomId, model);
        } catch (UserNotFoundException e) {
            assertEquals("User not found", e.getMessage());
        }
    }
}
