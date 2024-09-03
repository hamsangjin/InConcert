package com.inconcert.domain.chat.controller;

import com.inconcert.domain.chat.dto.NotificationMessageDTO;
import com.inconcert.domain.chat.dto.UserDTO;
import com.inconcert.domain.chat.service.ChatNotificationService;
import com.inconcert.domain.chat.service.ChatService;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatApiController {
    private final ChatService chatService;
    private final ChatNotificationService chatNotificationService;
    private final UserService userService;

    // 채팅방에 속한 유저 목록
    @GetMapping("/users/{chatRoomId}")
    public ResponseEntity<List<UserDTO>> getUsersInChatRoom(@PathVariable("chatRoomId") Long chatRoomId) {
        return chatService.getUserDTOsByChatRoomId(chatRoomId);
    }

    // host에게 동행 요청 전송
    @PostMapping("/request-join/{chatRoomId}")
    public ResponseEntity<String> requestJoinChatRoom(@PathVariable("chatRoomId") Long chatRoomId) {
        return chatService.requestJoinChatRoom(chatRoomId);
    }

    // 동행 요청 승인
    @PostMapping("/approve-join/{chatRoomId}/{notificationId}")
    public ResponseEntity<String> approveJoinRequest(@PathVariable("chatRoomId") Long chatRoomId,
                                                     @PathVariable("notificationId") Long notificationId) {
        return chatService.approveJoinRequest(chatRoomId, notificationId);
    }

    // 동행 요청 거절
    @PostMapping("/reject-join/{chatRoomId}/{notificationId}")
    public ResponseEntity<String> rejectJoinRequest(@PathVariable("chatRoomId") Long chatRoomId,
                                                    @PathVariable("notificationId") Long notificationId) {
        return chatService.rejectJoinRequest(chatRoomId, notificationId);
    }

    // 채팅방 나가기
    @PostMapping("/leave/{chatRoomId}")
    public ResponseEntity<String> leaveChatRoom(@PathVariable("chatRoomId") Long chatRoomId) {
        return chatService.leaveChatRoom(chatRoomId);
    }

    // 강퇴 요청
    @PostMapping("/kick/{chatRoomId}/{userId}")
    public ResponseEntity<String> kickUser(@PathVariable("chatRoomId") Long chatRoomId,
                                           @PathVariable("userId") Long userId) {
        return chatService.kickUserFromChatRoom(chatRoomId, userId);
    }

    // 1:1 채팅
    @PostMapping("/request-one-to-one/{receiverId}")
    public ResponseEntity<?> requestOneToOneChat(@PathVariable("receiverId") Long receiverId) {
        return chatService.createOneToOneChatRoom(receiverId);
    }

    // 알림 삭제
    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable("notificationId") Long notificationId) {
        return chatNotificationService.deleteNotification(notificationId);
    }

    // 알림 목록 가져오기 (거절 및 요청)
    @GetMapping("/notifications/requestlist")
    public ResponseEntity<List<NotificationMessageDTO>> getNotificationsForUser(@RequestParam("userId") Long userId) {
        return chatNotificationService.getNotificationMessageDTOsByUserId(userId);
    }

    // 메시지 보낸 유저의 프로필 이미지 가져오기
    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getUserProfile(@RequestParam("nickname") String nickname) {
        return userService.getProfileImageByNickname(nickname);
    }
}