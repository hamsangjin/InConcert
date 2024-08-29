package com.inconcert.domain.chat.controller;

import com.inconcert.domain.chat.dto.NotificationMessageDTO;
import com.inconcert.domain.chat.service.ChatNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class ChatNotificationController {
    private final ChatNotificationService chatNotificationService;

    @GetMapping("/requestlist")
    public ResponseEntity<List<NotificationMessageDTO>> getRequestList(@RequestParam("userId") Long userId) {
        return chatNotificationService.getNotificationMessageDTOsByUserId(userId);
    }
}