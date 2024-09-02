package com.inconcert.domain.chat;

import com.inconcert.domain.chat.controller.ChatApiController;
import com.inconcert.domain.chat.dto.NotificationMessageDTO;
import com.inconcert.domain.chat.dto.UserDTO;
import com.inconcert.domain.chat.service.ChatNotificationService;
import com.inconcert.domain.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChatApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    @Mock
    private ChatNotificationService chatNotificationService;

    @InjectMocks
    private ChatApiController chatApiController;

    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatApiController)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    void 채팅방_사용자_조회() throws Exception {
        // Given: 특정 채팅방에 속한 사용자 정보 설정
        Long chatRoomId = 1L;
        UserDTO userDTO = new UserDTO(1L, "username", "nickname", "profileImage");
        when(chatService.getUserDTOsByChatRoomId(chatRoomId))
                .thenReturn(ResponseEntity.ok(List.of(userDTO)));

        // When & Then: API 호출 후 기대 결과 검증
        mockMvc.perform(get("/api/chat/users/{chatRoomId}", chatRoomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("username"))
                .andExpect(jsonPath("$[0].nickname").value("nickname"));

        verify(chatService, times(1)).getUserDTOsByChatRoomId(chatRoomId);
    }

    @Test
    void 동행_요청_전송() throws Exception {
        // Given: 동행 요청 전송을 위한 데이터 설정
        Long chatRoomId = 1L;
        when(chatService.requestJoinChatRoom(chatRoomId)).thenReturn(ResponseEntity.ok("요청을 성공적으로 전송하였습니다."));

        // When & Then: API 호출 후 기대 결과 검증
        mockMvc.perform(post("/api/chat/request-join/{chatRoomId}", chatRoomId))
                .andExpect(status().isOk())
                .andExpect(content().string("요청을 성공적으로 전송하였습니다."));

        verify(chatService, times(1)).requestJoinChatRoom(chatRoomId);
    }

    @Test
    void 동행_요청_승인() throws Exception {
        // Given: 동행 요청 승인 데이터 설정
        Long chatRoomId = 1L;
        Long notificationId = 2L;
        when(chatService.approveJoinRequest(chatRoomId, notificationId)).thenReturn(ResponseEntity.ok("승인이 완료되었습니다."));

        // When & Then: API 호출 후 기대 결과 검증
        mockMvc.perform(post("/api/chat/approve-join/{chatRoomId}/{notificationId}", chatRoomId, notificationId))
                .andExpect(status().isOk())
                .andExpect(content().string("승인이 완료되었습니다."));

        verify(chatService, times(1)).approveJoinRequest(chatRoomId, notificationId);
    }

    @Test
    void 동행_요청_거절() throws Exception {
        // Given: 동행 요청 거절 데이터 설정
        Long chatRoomId = 1L;
        Long notificationId = 2L;
        when(chatService.rejectJoinRequest(chatRoomId, notificationId)).thenReturn(ResponseEntity.ok("요청이 거절되었습니다."));

        // When & Then: API 호출 후 기대 결과 검증
        mockMvc.perform(post("/api/chat/reject-join/{chatRoomId}/{notificationId}", chatRoomId, notificationId))
                .andExpect(status().isOk())
                .andExpect(content().string("요청이 거절되었습니다."));

        verify(chatService, times(1)).rejectJoinRequest(chatRoomId, notificationId);
    }

    @Test
    void 채팅방_나가기() throws Exception {
        // Given: 채팅방 나가기 요청 데이터 설정
        Long chatRoomId = 1L;
        when(chatService.leaveChatRoom(chatRoomId)).thenReturn(ResponseEntity.ok("채팅방을 나갔습니다."));

        // When & Then: API 호출 후 기대 결과 검증
        mockMvc.perform(post("/api/chat/leave/{chatRoomId}", chatRoomId))
                .andExpect(status().isOk())
                .andExpect(content().string("채팅방을 나갔습니다."));

        verify(chatService, times(1)).leaveChatRoom(chatRoomId);
    }

    @Test
    void 유저_강퇴() throws Exception {
        // Given: 유저 강퇴 요청 데이터 설정
        Long chatRoomId = 1L;
        Long userId = 2L;
        when(chatService.kickUserFromChatRoom(chatRoomId, userId)).thenReturn(ResponseEntity.ok("유저가 강퇴되었습니다."));

        // When & Then: API 호출 후 기대 결과 검증
        mockMvc.perform(post("/api/chat/kick/{chatRoomId}/{userId}", chatRoomId, userId))
                .andExpect(status().isOk())
                .andExpect(content().string("유저가 강퇴되었습니다."));

        verify(chatService, times(1)).kickUserFromChatRoom(chatRoomId, userId);
    }

    @Test
    void 일대일_채팅방_생성_요청() throws Exception {
        // Given: 일대일 채팅방 생성 요청 데이터 설정
        Long receiverId = 1L;
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().body("채팅방 생성 완료");
        when(chatService.createOneToOneChatRoom(receiverId))
                .thenReturn((ResponseEntity) responseEntity);

        // When & Then: API 호출 후 기대 결과 검증
        mockMvc.perform(post("/api/chat/request-one-to-one/{receiverId}", receiverId))
                .andExpect(status().isOk())
                .andExpect(content().string("채팅방 생성 완료"));

        verify(chatService, times(1)).createOneToOneChatRoom(receiverId);
    }

    @Test
    void 알림_삭제() throws Exception {
        // Given: 알림 삭제 요청 데이터 설정
        Long notificationId = 1L;
        when(chatNotificationService.deleteNotification(notificationId)).thenReturn(ResponseEntity.ok("알림이 삭제되었습니다."));

        // When & Then: API 호출 후 기대 결과 검증
        mockMvc.perform(delete("/api/chat/notifications/{notificationId}", notificationId))
                .andExpect(status().isOk())
                .andExpect(content().string("알림이 삭제되었습니다."));

        verify(chatNotificationService, times(1)).deleteNotification(notificationId);
    }

    @Test
    void 유저의_알림_목록_조회() throws Exception {
        // Given: 특정 유저의 알림 목록 조회 요청 데이터 설정
        Long userId = 1L;
        NotificationMessageDTO notification = new NotificationMessageDTO(1L, "content", 1L, 1L);
        when(chatNotificationService.getNotificationMessageDTOsByUserId(userId))
                .thenReturn(ResponseEntity.ok(List.of(notification)));

        // When & Then: API 호출 후 기대 결과 검증
        mockMvc.perform(get("/api/chat/notifications/requestlist")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].message").value("content"));

        verify(chatNotificationService, times(1)).getNotificationMessageDTOsByUserId(userId);
    }
}
