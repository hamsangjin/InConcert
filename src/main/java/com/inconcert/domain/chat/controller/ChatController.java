package com.inconcert.domain.chat.controller;

import com.inconcert.domain.chat.dto.ChatMessageDto;
import com.inconcert.domain.chat.dto.ChatRoomDto;
import com.inconcert.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    // 사용자의 채팅 목록 보기
    @GetMapping("/chat/list")
    public String getChatList( Model model) {

        List<ChatRoomDto> chatRooms = chatService.getChatRoomDtosByUserId();
        model.addAttribute("chatRooms", chatRooms);
        return "chat/list";
    }

    // 특정 채팅방 조회
    @GetMapping("/chat/{chatRoomId}")
    public String getChatRoom(@PathVariable("chatRoomId") Long chatRoomId, Model model) {
        ChatRoomDto chatRoom = chatService.getChatRoomDto(chatRoomId);
        List<ChatMessageDto> messages = chatService.getMessageDtosByChatRoom(chatRoomId);
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("messages", messages);
        return "chat/room";
    }
}
