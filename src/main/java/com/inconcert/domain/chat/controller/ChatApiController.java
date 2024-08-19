package com.inconcert.domain.chat.controller;

import com.inconcert.domain.chat.dto.NotificationMessage;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.chat.service.ChatNotificationService;
import com.inconcert.domain.chat.service.ChatService;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatApiController {
    private final ChatService chatService;
    private final UserService userService;
    private final ChatNotificationService chatNotificationService;

    // host에게 동행 요청 전송
    @PostMapping("/request-join/{chatRoomId}")
    public ResponseEntity<?> requestJoinChatRoom(@PathVariable("chatRoomId") Long chatRoomId) {
        User currentUser = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        try {
            chatService.requestJoinChatRoom(chatRoomId, currentUser);
            return ResponseEntity.ok("요청을 성공적으로 전송하였습니다.");
        }
        // 이미 모집이 완료된 경우
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 동행 요청 승인
    @PostMapping("/approve-join/{chatRoomId}/{notificationId}")
    public ResponseEntity<?> approveJoinRequest(@PathVariable("chatRoomId") Long chatRoomId, @PathVariable("notificationId") Long notificationId) {
        chatService.approveJoinRequest(chatRoomId, notificationId);
        return ResponseEntity.ok("승인이 완료되었습니다.");
    }
//    @PostMapping("/approve-join/{chatRoomId}/{userId}")
//    public ResponseEntity<?> approveJoinRequest(@PathVariable Long chatRoomId, @PathVariable Long userId) {
//        chatService.approveJoinRequest(chatRoomId, userId);
//        return ResponseEntity.ok("승인이 완료되었습니다.");
//    }

    // 동행 요청 거절
    @PostMapping("/reject-join/{chatRoomId}/{notificationId}")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable("chatRoomId") Long chatRoomId, @PathVariable("notificationId") Long notificationId) {
        chatService.rejectJoinRequest(chatRoomId, notificationId);
        return ResponseEntity.ok("채팅방 '" + chatService.findById(chatRoomId).getRoomName() + "'의 요청이 거절되었습니다.");
    }

    // 1:1 채팅
    @PostMapping("/request-one-to-one/{receiverId}")
    public ResponseEntity<?> requestOneToOneChat(@PathVariable("receiverId") Long receiverId) {
        try {
            ChatRoom chatRoom = chatService.createOneToOneChatRoom(receiverId);
            return ResponseEntity.ok(chatRoom.getId()); // 채팅방 ID 반환
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 이미 존재하는 경우 메시지 반환
        }
    }

    // 알림 삭제
    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable("notificationId") Long notificationId) {
        chatNotificationService.deleteNotification(notificationId);
        return ResponseEntity.ok("알림이 삭제되었습니다.");
    }

    // 알림 목록 가져오기 (거절 및 요청)
    @GetMapping("/notifications/requestlist")
    public ResponseEntity<List<NotificationMessage>> getNotificationsForUser(@RequestParam("userId") Long userId) {
        List<NotificationMessage> notifications = chatNotificationService.findRejectedNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }
}
