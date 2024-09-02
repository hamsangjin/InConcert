package com.inconcert.domain.chat;

import com.inconcert.domain.chat.controller.ChatNotificationController;
import com.inconcert.domain.chat.dto.NotificationMessageDTO;
import com.inconcert.domain.chat.service.ChatNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChatNotificationControllerTest {

    @Mock
    private ChatNotificationService chatNotificationService;

    @InjectMocks
    private ChatNotificationController chatNotificationController;

    private MockMvc mockMvc;

    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatNotificationController).build();
    }

    @Test
    void 사용자_알림_요청_목록_가져오기() throws Exception {
        // Given: 특정 사용자에 대한 알림 목록을 반환하도록 설정
        Long userId = 1L;
        NotificationMessageDTO notification1 = new NotificationMessageDTO(1L, "첫 번째 알림", 1L, userId);
        NotificationMessageDTO notification2 = new NotificationMessageDTO(2L, "두 번째 알림", 1L, userId);
        List<NotificationMessageDTO> notifications = Arrays.asList(notification1, notification2);

        // When: 알림 요청 목록 조회 요청
        when(chatNotificationService.getNotificationMessageDTOsByUserId(anyLong()))
                .thenReturn(ResponseEntity.ok(notifications));

        // Then: 결과 검증
        mockMvc.perform(get("/api/notifications/requestlist")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(notification1.getId()))
                .andExpect(jsonPath("$[0].message").value(notification1.getMessage()))
                .andExpect(jsonPath("$[1].id").value(notification2.getId()))
                .andExpect(jsonPath("$[1].message").value(notification2.getMessage()));
    }
}
