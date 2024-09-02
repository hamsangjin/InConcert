package com.inconcert.domain.chat;

import com.inconcert.domain.chat.controller.ChatWebSocketController;
import com.inconcert.domain.chat.dto.ChatMessageDTO;
import com.inconcert.domain.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatWebSocketControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatWebSocketController chatWebSocketController;

    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 사용자_입장_처리() {
        // Given: 입장 메시지 설정
        ChatMessageDTO message = ChatMessageDTO.builder()
                .chatRoomId(1L)
                .nickname("testUser")
                .type(ChatMessageDTO.MessageType.ENTER)
                .build();

        // When: 사용자가 채팅방에 입장할 때
        chatWebSocketController.enterUser(message);

        // Then: 입장 메시지를 해당 채팅방의 모든 사용자에게 전송
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/chat/room/" + message.getChatRoomId()), any(ChatMessageDTO.class));
    }

    @Test
    void 메시지_전송_처리() {
        // Given: 채팅 메시지 설정
        ChatMessageDTO message = ChatMessageDTO.builder()
                .chatRoomId(1L)
                .nickname("testUser")
                .message("Hello, World!")
                .type(ChatMessageDTO.MessageType.CHAT)
                .build();

        // When: 사용자가 채팅방에서 메시지 전송
        chatWebSocketController.sendMessage(message);

        // Then: 서비스 호출을 통해 메시지 전송 처리
        verify(chatService, times(1)).sendMessage(message);
    }

    @Test
    void 사용자_퇴장_처리() {
        // Given: 퇴장 메시지 설정
        ChatMessageDTO message = ChatMessageDTO.builder()
                .chatRoomId(1L)
                .nickname("testUser")
                .type(ChatMessageDTO.MessageType.LEAVE)
                .build();

        // When: 사용자가 채팅방에서 퇴장할 때
        chatWebSocketController.leaveUser(message);

        // Then: 퇴장 메시지를 해당 채팅방의 모든 사용자에게 전송
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/chat/room/" + message.getChatRoomId()), any(ChatMessageDTO.class));
    }
}
