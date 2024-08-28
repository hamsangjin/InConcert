package com.inconcert.domain.chat.service;

import com.inconcert.domain.chat.dto.ChatMessageDTO;
import com.inconcert.domain.chat.dto.ChatRoomDTO;
import com.inconcert.domain.chat.dto.UserDTO;
import com.inconcert.domain.chat.entity.ChatMessage;
import com.inconcert.domain.chat.entity.ChatNotification;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.chat.repository.ChatMessageRepository;
import com.inconcert.domain.chat.repository.ChatRoomRepository;
import com.inconcert.domain.chat.repository.ChatNotificationRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatNotificationRepository chatNotificationRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ChatNotificationService chatNotificationService;
    private final SimpMessagingTemplate messagingTemplate;

    // 사용자의 채팅방 목록
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getChatRoomDTOsByUserId() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 네이티브 쿼리에서 DTO를 생성해서 담아주지 못하기 때문에, List<Map<String, Object>>로 반환받음
        // 그 후 각각 타입변환하고, null처리까지 한 후 List<ChatRoomDTO>로 반환
        return chatRoomRepository.getChatRoomDTOsByUserId(user.getId()).stream()
                .map(chatRoomDTOS -> ChatRoomDTO.builder()
                        .id((Long) chatRoomDTOS.get("chatRoomId"))
                        .roomName((String) chatRoomDTOS.get("roomName"))
                        .hostUserId((Long) chatRoomDTOS.get("hostUserId"))
                        .userCount(((Long) chatRoomDTOS.get("userCount")).intValue())
                        .timeSince(chatRoomDTOS.get("timeSince") != null ? (String) chatRoomDTOS.get("timeSince") : null)
                        .diffTime(chatRoomDTOS.get("diffTime") != null ? ((Long) chatRoomDTOS.get("diffTime")).intValue() : 0)
                        .build())
                .toList();
    }

    // 특정 채팅방 조회
    @Transactional(readOnly = true)
    public ChatRoomDTO getChatRoomDTOByChatRoomId(Long chatRoomId) {
        return chatRoomRepository.getChatRoomDTOById(chatRoomId);
    }

    // 채팅방에 속한 유저 목록
    @Transactional(readOnly = true)
    public List<UserDTO> getUserDTOsByChatRoomId(Long chatRoomId) {
        return chatRoomRepository.findAllById(chatRoomId);
    }

    // 유저가 채팅방에 있는지 확인하는 메소드
    @Transactional(readOnly = true)
    public boolean isExistUser(Long chatRoomId) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        return getUserDTOsByChatRoomId(chatRoomId).stream()
                .anyMatch(userDto -> userDto.getId().equals(user.getId()));
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomDTO createChatRoom(String roomName) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .hostUser(user)
                .users(Arrays.asList(user))
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .hostUserId(chatRoom.getHostUser().getId())
                .userCount(chatRoom.getUsers().size())
                .build();
    }

    // 동행 요청 Host에게 전송
    @Transactional
    public ResponseEntity<String> requestJoinChatRoom(Long chatRoomId) {
        User requestingUser = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));
        Post post = chatRoom.getPost();

        // 이미 채팅방에 사용자가 속해 있는 경우
        if (chatRoom.getUsers().contains(requestingUser)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ExceptionMessage.ALREADY_IN_CHATROOM.getMessage());
        }
        // 현재 인원이 matchCount와 일치하거나 초과하는지 확인
        if (chatRoom.getUsers().size() >= post.getMatchCount()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.ALREADY_FULL_CHATROOM.getMessage());
        }
        // 이미 신청한 경우
        if(chatNotificationRepository.existsByRequestUserAndChatRoom(requestingUser, chatRoom)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ExceptionMessage.ALREADY_APPLIED_TO_CHATROOM.getMessage());
        }

        User hostUser = chatRoom.getHostUser();

        // 알림 전송
        chatNotificationService.sendJoinRequestNotification(hostUser, chatRoom, requestingUser);

        return ResponseEntity.ok("요청을 성공적으로 전송하였습니다.");
    }

    // 입장 승인
    @Transactional
    public ResponseEntity<String> approveJoinRequest(Long chatRoomId, Long notificationId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        ChatNotification chatNotification = chatNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ChatNotificationNotFoundException(ExceptionMessage.CHAT_NOTIFICATION_NOT_FOUND.getMessage()));

        User requestUser = chatNotification.getRequestUser();

        // 요청자가 채팅방에 없을 떄 추가
        if (!chatRoom.getUsers().contains(requestUser)) {
            chatRoom.addUser(requestUser);
            chatRoomRepository.save(chatRoom);

            // 1대1 채팅이 아닌 경우에만 입장 메시지 전송
            if(chatRoom.getPost() != null){
                ChatMessageDTO enterMessage = ChatMessageDTO.builder()
                        .chatRoomId(chatRoomId)
                        .username(requestUser.getUsername())
                        .nickname(requestUser.getNickname())
                        .message(requestUser.getNickname() + "님이 입장하셨습니다.")
                        .type(ChatMessageDTO.MessageType.ENTER)
                        .isNotice(true)
                        .build();

                ChatMessage saveMessage = ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(requestUser)
                        .message(enterMessage.getMessage())
                        .isNotice(true)
                        .build();

                messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId, enterMessage);
                chatMessageRepository.save(saveMessage);
            }
        }

        // 사용자에게 채팅방 입장이 승인되었음을 알림
        chatNotificationService.sendJoinApprovalNotification(requestUser, chatRoom, notificationId);

        return ResponseEntity.ok("승인이 완료되었습니다.");
    }

    // 사용자가 채팅방에서 퇴장할 때 처리
    @Transactional
    public ResponseEntity<String> leaveChatRoom(Long chatRoomId) {
        User leavingUser = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        // host는 2명 이상일 때 나갈 수 없음
        if(chatRoom.getPost() != null && chatRoom.getHostUser().equals(leavingUser) && chatRoom.getUsers().size() >= 2) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ExceptionMessage.HOST_EXIT.getMessage());
        }
        chatRoom.removeUser(leavingUser);   // host가 아니거나, host가 채팅방에 혼자 있는 경우

        // 채팅방 인원이 0명일 경우 post와 채팅방 삭제
        if (chatRoom.getUsers().isEmpty()) {
            chatRoomRepository.deleteById(chatRoomId);
        }
        else {
            // 1대1 채팅방이 아닌 채팅방에서 나간 경우 퇴장 메시지 전송
            if(chatRoom.getPost() != null) {
                chatRoomRepository.save(chatRoom);
                ChatMessageDTO leaveMessage = ChatMessageDTO.builder()
                        .chatRoomId(chatRoomId)
                        .username(leavingUser.getUsername())
                        .nickname(leavingUser.getNickname())
                        .message(leavingUser.getNickname() + "님이 퇴장하셨습니다.")
                        .type(ChatMessageDTO.MessageType.LEAVE)
                        .isNotice(true)
                        .build();

                ChatMessage saveMessage = ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(leavingUser)
                        .message(leaveMessage.getMessage())
                        .isNotice(true)
                        .build();
                chatMessageRepository.save(saveMessage);

                messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId, leaveMessage);
            }
        }
        return ResponseEntity.ok("채팅방을 나갔습니다.");
    }

    // 강퇴
    @Transactional
    public ResponseEntity<String> kickUserFromChatRoom(Long chatRoomId, Long kickedUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));
        User kickedUser = userRepository.findById(kickedUserId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 호스트가 아니면 강퇴할 수 없음
        if (!chatRoom.getHostUser().getUsername().equals((userService.getAuthenticatedUser().get().getUsername()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ExceptionMessage.KICK_NOT_ALLOWED.getMessage());
        }

        // 완전히 나간 경우 퇴장 메시지 전송
        ChatMessageDTO kickedMessage = ChatMessageDTO.builder()
                .chatRoomId(chatRoomId)
                .username(kickedUser.getUsername())
                .nickname(kickedUser.getNickname())
                .message(kickedUser.getNickname() + "님이 강퇴되었습니다.")
                .type(ChatMessageDTO.MessageType.LEAVE)
                .isNotice(true)
                .build();

        ChatMessage saveMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(kickedUser)
                .message(kickedMessage.getMessage())
                .isNotice(true)
                .build();
        chatMessageRepository.save(saveMessage);

        messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId, kickedMessage);
        messagingTemplate.convertAndSend("/topic/chat/kicked/" + kickedUserId, "채팅방에서 강퇴되었습니다.");

        // 유저 강퇴
        chatRoom.removeUser(kickedUser);
        chatRoomRepository.save(chatRoom);

        return ResponseEntity.ok("유저가 강퇴되었습니다.");
    }

    // 요청 거절
    @Transactional
    public ResponseEntity<String> rejectJoinRequest(Long chatRoomId, Long notificationId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        ChatNotification chatNotification = chatNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ChatNotificationNotFoundException(ExceptionMessage.CHAT_NOTIFICATION_NOT_FOUND.getMessage()));

        User requestUser = chatNotification.getRequestUser();

        // 요청자에게 거절 알림 전송
        chatNotificationService.sendJoinRejectionNotification(requestUser, chatRoom, notificationId);

        return ResponseEntity.ok("\"" + chatRoom.getRoomName() + "\" 채팅방의 요청이 거절되었습니다.");
    }


    // 채팅방 메시지 가져오기
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatMessageDTOsByChatRoomId(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));
        return chatMessageRepository.findByChatRoom(chatRoom.getId());
    }

    // 메시지 전송
    @Transactional
    public void sendMessage(ChatMessageDTO message) {
        User user = userRepository.findByUsername(message.getUsername())
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        ChatRoom chatRoom = chatRoomRepository.findById(message.getChatRoomId())
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .message(message.getMessage())
                .isNotice(false)
                .build();

        message.setType(ChatMessageDTO.MessageType.CHAT);
        // 메시지를 해당 채팅방의 모든 사용자에게 전송
        messagingTemplate.convertAndSend("/topic/chat/room/" + message.getChatRoomId(), message);

        chatMessageRepository.save(chatMessage);
    }

    // 1:1 채팅방 생성
    @Transactional
    public ResponseEntity<?> createOneToOneChatRoom(Long receiverId) {
        User requestingUser = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 이미 요청자와 수신자 사이의 1:1 채팅방이 존재하는지 확인
        List<ChatRoom> existingRooms = chatRoomRepository.findByUsersContainsAndUsersContainsAndPostIsNull(requestingUser, receiver);

        if (!existingRooms.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ExceptionMessage.ALREADY_IN_CHATROOM.getMessage());
        } else if(receiverId.equals(requestingUser.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMessage.SELF_CHAT.getMessage());
        }

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName("1:1 채팅: " + requestingUser.getNickname() + ", " + receiver.getNickname())
                .build();

        chatRoom.addUser(requestingUser); // 요청자 추가
        chatRoom.addUser(receiver); // 수신자 추가

        return ResponseEntity.ok(chatRoomRepository.save(chatRoom).getId());
    }
}