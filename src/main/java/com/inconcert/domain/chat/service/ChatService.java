package com.inconcert.domain.chat.service;

import com.inconcert.domain.chat.dto.ChatMessageDto;
import com.inconcert.domain.chat.dto.ChatRoomDto;
import com.inconcert.domain.chat.entity.ChatMessage;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.chat.repository.ChatMessageRepository;
import com.inconcert.domain.chat.repository.ChatRoomRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 전체 채팅방 조회
    public List<ChatRoomDto> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream()
                .map(this::convertToChatRoomDto)
                .collect(Collectors.toList());
    }

    // 특정 채팅방 조회
    public ChatRoomDto getChatRoomDto(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        return convertToChatRoomDto(chatRoom);
    }

    // 사용자의 채팅방 목록 가져오기
    public List<ChatRoomDto> getChatRoomDtosByUserId() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(user.getId());
        return chatRooms.stream()
                .map(this::convertToChatRoomDto)
                .collect(Collectors.toList());
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomDto createChatRoom(String roomName) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .users(new ArrayList<>())
                .hostUser(user)
                .build();

        chatRoom.addUser(user);
        chatRoom = chatRoomRepository.save(chatRoom);

        return convertToChatRoomDto(chatRoom);
    }

    //동행 요청 Host 에게 전송
    public void requestJoinChatRoom(Long chatRoomId, User requestingUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

        User hostUser = chatRoom.getHostUser();

        // 알림 전송 로직
        notificationService.sendJoinRequestNotification(hostUser, chatRoom, requestingUser);
    }


    //입장 승인 로직
    public void approveJoinRequest(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        chatRoom.addUser(user);
        chatRoomRepository.save(chatRoom);

        // 사용자에게 채팅방 입장이 승인되었음을 알림
        notificationService.sendJoinApprovalNotification(user, chatRoom);
    }

    // 채팅 참여
    @Transactional
    public void joinChatRoom(Long chatRoomId) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        if (!chatRoom.getUsers().contains(user)) {
            chatRoom.addUser(user);
            chatRoomRepository.save(chatRoom);
        }
    }

    // 채팅방 메시지 가져오기
    public List<ChatMessageDto> getMessageDtosByChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        List<ChatMessage> messages = chatMessageRepository.findByChatRoom(chatRoom);
        return messages.stream()
                .map(this::convertToChatMessageDto)
                .collect(Collectors.toList());
    }

    // 메시지 보내기
    @Transactional
    public void sendMessage(Long chatRoomId, String message) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .message(message)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        chatMessageRepository.save(chatMessage);
    }

    // 모집 종료 처리
    @Transactional
    public void closeChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        // 모집 종료 처리 로직
    }

    private ChatRoomDto convertToChatRoomDto(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .hostUserId(chatRoom.getHostUser().getId())
                .userCount(chatRoom.getUsers().size())
                .build();
    }

    private ChatMessageDto convertToChatMessageDto(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .senderId(chatMessage.getSender().getId())
                .message(chatMessage.getMessage())
                .timestamp(chatMessage.getTimestamp())
                .build();
    }


}