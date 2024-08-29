package com.inconcert.domain.notification.controller;

import com.inconcert.domain.notification.dto.NotificationDTO;
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
        return notificationService.getCurrentKeywords();
    }

    @PostMapping("/keyword")
    public ResponseEntity<String> addKeyword(@RequestParam("keyword") String keyword) {
        return notificationService.addKeyword(keyword);
    }

    @DeleteMapping("/keyword")
    public ResponseEntity<String> removeKeyword(@RequestParam("keyword") String keyword) {
        return notificationService.removeKeyword(keyword);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable("id") Long id) {
        return notificationService.markAsRead(id);
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByType(@PathVariable("type") String type) {
        return notificationService.getNotificationsByTypeAndUser(type);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteNotification(@PathVariable("id") Long id) {
        return notificationService.deleteNotification(id);
    }
}
