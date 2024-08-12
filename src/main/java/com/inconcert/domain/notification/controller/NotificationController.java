package com.inconcert.domain.notification.controller;

import com.inconcert.domain.notification.dto.NotificationDto;
import com.inconcert.domain.notification.entity.Notification;
import com.inconcert.domain.notification.service.NotificationService;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    @PostMapping("/keyword")
    public ResponseEntity<?> setKeyword(@RequestParam String keyword) {
        try {
            User user = userService.getAuthenticatedUser()
                    .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
            notificationService.setKeyword(user.getId(), keyword);
            return ResponseEntity.ok("키워드 등록 완료");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("키워드 등록 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("키워드 등록 실패: " + e.getMessage());
        }
    }

    @GetMapping("/stream")
    public SseEmitter streamNotifications() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자가 아닙니다."));
        return notificationService.createSseEmitter(user.getId());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자가 아닙니다."));
        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        List<NotificationDto> notificationDtos = notifications.stream()
                .map(notificationService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notificationDtos);
    }

    @GetMapping("/current-keywords")
    public ResponseEntity<Set<String>> getCurrentKeywords() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자가 아닙니다."));
        Set<String> keywords = notificationService.getCurrentKeywords(user.getId());
        return ResponseEntity.ok(keywords);
    }

    @DeleteMapping("/keyword")
    public ResponseEntity<?> removeKeyword(@RequestParam String keyword) {
        try {
            User user = userService.getAuthenticatedUser()
                    .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
            notificationService.removeKeyword(user.getId(), keyword);
            return ResponseEntity.ok("키워드가 성공적으로 제거되었습니다.");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("키워드 제거 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("키워드 제거 실패: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알림 상태 변경 실패: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationDto>> getAllNotifications() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자가 아닙니다."));
        List<Notification> notifications = notificationService.getAllNotifications(user);
        List<NotificationDto> notificationDtos = notifications.stream()
                .map(notificationService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notificationDtos);
    }
}
