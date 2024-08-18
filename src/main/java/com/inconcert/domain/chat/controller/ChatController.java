package com.inconcert.domain.chat.controller;

import com.inconcert.domain.chat.dto.ChatMessageDto;
import com.inconcert.domain.chat.dto.ChatRoomDto;
import com.inconcert.domain.chat.service.ChatService;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.UserNotFoundException;
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
        List<ChatRoomDto> chatRooms = chatService.getChatRoomDtosByUserId();
        model.addAttribute("chatRooms", chatRooms);
        return "chat/list";
    }

    // 채팅 요청 알림 목록 보기
    @GetMapping("/request")
    public String requestList(Model model) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
        model.addAttribute("user", user);
        return "/chat/requests";
    }

    // 특정 채팅방 조회
    @GetMapping("/{chatRoomId}")
    public String getChatRoom(@PathVariable("chatRoomId") Long chatRoomId, Model model) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
        ChatRoomDto chatRoom = chatService.getChatRoomDto(chatRoomId);
        List<ChatMessageDto> messages = chatService.getMessageDtosByChatRoom(chatRoomId);
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("messages", messages);
        model.addAttribute("user", user);
        return "chat/room";
    }

    // ----------

    // 채팅방 생성 폼
//    @GetMapping("/chat/{userId}/newform")
//    public String getChatRoomForm(@PathVariable("userId") Long userId, Model model) {
//        model.addAttribute("userId", userId);
//        return "chat/newform";
//    }
//    @GetMapping("/new")
//    public String getChatRoomForm(Model model) {
//        return "chat/newform";
//    }

    // 채팅방 생성
//    @PostMapping("/chat/{userId}/new")
//    public String createChatRoom(@PathVariable("userId") Long userId, @RequestParam String roomName, RedirectAttributes redirectAttributes) {
//        ChatRoomDto chatRoom = chatService.createChatRoom(roomName);
//        redirectAttributes.addAttribute("chatRoomId", chatRoom.getId());
//        return "redirect:/chat/{userId}/{chatRoomId}";
//    }
//    @PostMapping("/new")
//    public String createChatRoom(@RequestParam String roomName, RedirectAttributes redirectAttributes) {
//        ChatRoomDto chatRoom = chatService.createChatRoom(roomName);
//        redirectAttributes.addAttribute("chatRoomId", chatRoom.getId());
//        return "redirect:/chat/{chatRoomId}";
//    }

    // 채팅 내용 보기
//    @GetMapping("/chat/{userId}/{chatRoomId}")
//    public String getChatRoom(@PathVariable("userId") Long userId, @PathVariable("chatRoomId") Long chatRoomId, Model model) {
//        ChatRoomDto chatRoom = chatService.getChatRoomDto(chatRoomId);
//        List<ChatMessageDto> messages = chatService.getMessageDtosByChatRoom(chatRoomId);
//        model.addAttribute("chatRoom", chatRoom);
//        model.addAttribute("messages", messages);
//        model.addAttribute("userId", userId);
//        return "chat/room";
//    }

    // 채팅 전송
//    @PostMapping("/chat/{userId}/{chatRoomId}/send")
//    public String sendMessage(@PathVariable("userId") Long userId, @PathVariable("chatRoomId") Long chatRoomId, @RequestParam String message, RedirectAttributes redirectAttributes) {
//        chatService.sendMessage(chatRoomId, message);
//        redirectAttributes.addAttribute("chatRoomId", chatRoomId);
//        return "redirect:/chat/{userId}/{chatRoomId}";
//    }
//    @PostMapping("/chat/{chatRoomId}/send")
//    public String sendMessage(@PathVariable("chatRoomId") Long chatRoomId, @RequestParam String message, RedirectAttributes redirectAttributes) {
//        chatService.sendMessage(chatRoomId, message);
//        redirectAttributes.addAttribute("chatRoomId", chatRoomId);
//        return "redirect:/chat/{chatRoomId}";
//    }
//    @MessageMapping("/{chatRoomId}/send")
//    public void sendMessage(@DestinationVariable("chatRoomId") Long chatRoomId, @Payload ChatMessageDto message) {
////        chatService.sendMessage(chatRoomId, message.getMessage());
//        chatService.sendMessage(message.getChatRoomId(), /*message.getSenderId(),*/ message.getMessage());
//        messagingTemplate.convertAndSend("/ws/chat/" + chatRoomId, message);
//    }

    // 모집 종료
//    @PostMapping("/{chatRoomId}/close")
//    public String closeChatRoom(@PathVariable("chatRoomId") Long chatRoomId, RedirectAttributes redirectAttributes) {
//        chatService.closeChatRoom(chatRoomId);
//        return "redirect:/chat/{chatRoomId}";
//    }
}