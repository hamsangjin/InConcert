package com.inconcert.domain.chat.controller;


import com.inconcert.domain.chat.service.ChatService;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;
    private final UserService userService;

    @PostMapping("/request-join/{chatRoomId}")
    public ResponseEntity<?> requestJoinChatRoom(@PathVariable Long chatRoomId) {
        User currentUser = userService.getAuthenticatedUser().orElseThrow(()->new UserNotFoundException("User를 찾을 수 없습니다."));
        System.out.println("=================================="+ currentUser.getName());
        chatService.requestJoinChatRoom(chatRoomId, currentUser);
        return ResponseEntity.ok("요청을 성공적으로 전송하였습니다.");
    }


    @PostMapping("/approve-join/{chatRoomId}/{userId}")
    public ResponseEntity<?> approveJoinRequest(@PathVariable Long chatRoomId, @PathVariable Long userId) {
        chatService.approveJoinRequest(chatRoomId, userId);
        return ResponseEntity.ok("승인 완료.");
    }
}
