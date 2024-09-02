package com.inconcert.domain.notification;

import com.inconcert.domain.notification.controller.NotificationController;
import com.inconcert.domain.notification.dto.NotificationDTO;
import com.inconcert.domain.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))  // UTF-8 인코딩 필터 추가
                .build();
    }

    @Test
    void 알림_스트림_테스트() throws Exception {
        // Given
        SseEmitter sseEmitter = new SseEmitter();
        when(notificationService.createSseEmitter()).thenReturn(sseEmitter);

        // When & Then
        mockMvc.perform(get("/notifications/stream"))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).createSseEmitter();
    }

    @Test
    void 현재_키워드_가져오기_테스트() throws Exception {
        // Given
        Set<String> keywords = new HashSet<>(Arrays.asList("keyword1", "keyword2"));
        when(notificationService.getCurrentKeywords()).thenReturn(ResponseEntity.ok(keywords));

        // When & Then
        mockMvc.perform(get("/notifications/current-keywords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("keyword1"))
                .andExpect(jsonPath("$[1]").value("keyword2"));

        verify(notificationService, times(1)).getCurrentKeywords();
    }

    @Test
    void 키워드_추가_테스트() throws Exception {
        // Given
        String keyword = "testKeyword";
        when(notificationService.addKeyword(keyword)).thenReturn(ResponseEntity.ok("키워드 등록 완료"));

        // When & Then
        mockMvc.perform(post("/notifications/keyword")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(content().string("키워드 등록 완료"));

        verify(notificationService, times(1)).addKeyword(keyword);
    }

    @Test
    void 키워드_제거_테스트() throws Exception {
        // Given
        String keyword = "testKeyword";
        when(notificationService.removeKeyword(keyword)).thenReturn(ResponseEntity.ok("키워드가 성공적으로 제거되었습니다."));

        // When & Then
        mockMvc.perform(delete("/notifications/keyword")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(content().string("키워드가 성공적으로 제거되었습니다."));

        verify(notificationService, times(1)).removeKeyword(keyword);
    }

    @Test
    void 알림_읽음_표시_테스트() throws Exception {
        // Given
        Long notificationId = 1L;
        when(notificationService.markAsRead(notificationId)).thenReturn(ResponseEntity.ok("알림을 읽었습니다."));

        // When & Then
        mockMvc.perform(post("/notifications/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(content().string("알림을 읽었습니다."));

        verify(notificationService, times(1)).markAsRead(notificationId);
    }

    @Test
    void 모든_알림_가져오기_테스트() throws Exception {
        // Given
        NotificationDTO notification1 = new NotificationDTO(1L, "keyword1", "message1", false, "type1", null, "category1", "postCategory1", 1L);
        NotificationDTO notification2 = new NotificationDTO(2L, "keyword2", "message2", true, "type2", null, "category2", "postCategory2", 2L);
        List<NotificationDTO> notifications = Arrays.asList(notification1, notification2);
        when(notificationService.getAllNotifications()).thenReturn(ResponseEntity.ok(notifications));

        // When & Then
        mockMvc.perform(get("/notifications/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].message").value("message1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].message").value("message2"));

        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    void 특정_타입_알림_가져오기_테스트() throws Exception {
        // Given
        String type = "comment";
        NotificationDTO notification = new NotificationDTO(1L, "keyword", "message", false, "comment", null, "categoryTitle", "postCategoryTitle", 1L);
        List<NotificationDTO> notifications = List.of(notification);
        when(notificationService.getNotificationsByTypeAndUser(type)).thenReturn(ResponseEntity.ok(notifications));  // 수정된 메서드 사용

        // When & Then
        mockMvc.perform(get("/notifications/{type}", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("comment"));

        verify(notificationService, times(1)).getNotificationsByTypeAndUser(type);  // 수정된 메서드 사용
    }

    @Test
    void 알림_삭제_테스트() throws Exception {
        // Given
        Long notificationId = 1L;
        when(notificationService.deleteNotification(notificationId)).thenReturn(ResponseEntity.ok("알림이 삭제되었습니다."));

        // When & Then
        mockMvc.perform(delete("/notifications/{id}/delete", notificationId))
                .andExpect(status().isOk())
                .andExpect(content().string("알림이 삭제되었습니다."));

        verify(notificationService, times(1)).deleteNotification(notificationId);
    }
}
