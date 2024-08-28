package com.inconcert.domain.chat.controller;

import com.inconcert.domain.chat.dto.ChatMessageDTO;
import com.inconcert.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 사용자가 채팅방에 입장할 때
    @MessageMapping("/chat/enterUser")
    public void enterUser(@Payload ChatMessageDTO message) {
        message.setType(ChatMessageDTO.MessageType.ENTER);
        message.setMessage(message.getNickname() + "님이 입장하셨습니다.");

        // 입장 메시지를 해당 채팅방의 모든 사용자에게 전송
        messagingTemplate.convertAndSend("/topic/chat/room/" + message.getChatRoomId(), message);
    }

    // 사용자가 채팅방에서 메시지 전송
    @MessageMapping("/chat/sendMessage")
    public void sendMessage(@Payload ChatMessageDTO message) {
        chatService.sendMessage(message);
    }

    // 사용자가 채팅방에서 퇴장할 때
    @MessageMapping("/chat/leaveUser")
    public void leaveUser(@Payload ChatMessageDTO message) {
        message.setType(ChatMessageDTO.MessageType.LEAVE);
        message.setMessage(message.getNickname() + "님이 퇴장하셨습니다.");

        // 퇴장 메시지를 해당 채팅방의 모든 사용자에게 전송
        messagingTemplate.convertAndSend("/topic/chat/room/" + message.getChatRoomId(), message);
    }
}