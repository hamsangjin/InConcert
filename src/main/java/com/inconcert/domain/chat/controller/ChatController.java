package com.inconcert.domain.chat.controller;

import com.inconcert.domain.chat.dto.ChatMessageDTO;
import com.inconcert.domain.chat.dto.ChatRoomDTO;
import com.inconcert.domain.chat.service.ChatService;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    // 사용자의 채팅 목록 보기
    @GetMapping("/list")
    public String getChatList(Model model) {
        List<ChatRoomDTO> chatRooms = chatService.getChatRoomDTOsByUserId();
        model.addAttribute("chatRooms", chatRooms);
        return "chat/list";
    }

    // 채팅 요청 알림 목록 보기
    @GetMapping("/request")
    public String requestList(Model model) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        model.addAttribute("user", user);
        return "chat/requests";
    }

    // 특정 채팅방 조회
    @GetMapping("/{chatRoomId}")
    public String getChatRoom(@PathVariable("chatRoomId") Long chatRoomId, Model model) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 유저가 채팅방에 없으면 채팅방 리스트로 리다이렉트
        if(!chatService.isExistUser(chatRoomId)) return "redirect:/chat/list";

        ChatRoomDTO chatRoom = chatService.getChatRoomDTOByChatRoomId(chatRoomId);
        List<ChatMessageDTO> messages = chatService.getChatMessageDTOsByChatRoomId(chatRoomId);

        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("messages", messages);
        model.addAttribute("user", user);
        return "chat/room";
    }
}