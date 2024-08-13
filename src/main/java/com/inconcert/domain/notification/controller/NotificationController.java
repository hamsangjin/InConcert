package com.inconcert.domain.notification.controller;

import com.inconcert.domain.notification.dto.NotificationDto;
import com.inconcert.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/stream")
    public SseEmitter streamNotifications() {
        return notificationService.createSseEmitter();
    }

    @GetMapping("/current-keywords")
    public ResponseEntity<Set<String>> getCurrentKeywords() {
        return ResponseEntity.ok(notificationService.getCurrentKeywords());
    }

    @PostMapping("/keyword")
    public ResponseEntity<?> setKeyword(@RequestParam("keyword") String keyword) {
        notificationService.setKeyword(keyword);
        return ResponseEntity.ok("키워드 등록 완료");
    }

    @DeleteMapping("/keyword")
    public ResponseEntity<?> removeKeyword(@RequestParam("keyword") String keyword) {
        notificationService.removeKeyword(keyword);
        return ResponseEntity.ok("키워드가 성공적으로 제거되었습니다.");
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationDto>> getAllNotifications() {
        List<NotificationDto> notificationDtos = notificationService.getAllNotifications();
        return ResponseEntity.ok(notificationDtos);
    }

    @GetMapping("/{type}")
    public List<NotificationDto> getNotificationsByType(@PathVariable("type") String type) {
        return notificationService.getNotificationsByTypeAndUser(type);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteNotification(@PathVariable("id") Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }
}
