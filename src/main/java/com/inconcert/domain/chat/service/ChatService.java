package com.inconcert.domain.chat.service;

import com.inconcert.domain.chat.dto.ChatMessageDTO;
import com.inconcert.domain.chat.dto.ChatRoomDTO;
import com.inconcert.domain.chat.dto.UserDto;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public ChatRoom getChatRoomById(Long id) {
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));
    }

    // 사용자의 채팅방 목록
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getChatRoomDtosByUserId() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(user.getId());

        return chatRooms.stream()
                .map(this::convertToChatRoomDTO)
                .collect(Collectors.toList());
    }

    // 특정 채팅방 조회
    @Transactional(readOnly = true)
    public ChatRoomDTO getChatRoomDto(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .hostUserId(chatRoom.getHostUser().getId())
                .userCount(chatRoom.getUsers().size())
                .build();
    }

    // 채팅방에 속한 유저 목록
    @Transactional(readOnly = true)
    public List<UserDto> getUsersInChatRoom(Long chatRoomId) {
        List<User> users = chatRoomRepository.findAllById(chatRoomId);
        // 유저 목록을 UserDto로 변환
        return users.stream()
                .map(UserDto::from)
                .collect(Collectors.toList());
    }

    // 유저가 채팅방에 있는지 확인하는 메소드
    @Transactional(readOnly = true)
    public boolean isExistUser(Long chatRoomId) {
        User loggedInUser = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        List<UserDto> usersInChatRoom = getUsersInChatRoom(chatRoomId);  // 채팅방 유저 목록 가져오기
        for (UserDto userDto : usersInChatRoom) {
            if (userDto.getId().equals(loggedInUser.getId())) {
                return true;  // 사용자가 채팅방에 있음
            }
        }
        return false;  // 사용자가 채팅방에 없음
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomDTO createChatRoom(String roomName) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .hostUser(user)
                .users(new ArrayList<>())
                .build();

        chatRoom.addUser(user);
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
    public void requestJoinChatRoom(Long chatRoomId, User requestingUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        Post post = chatRoom.getPost();

        // 이미 채팅방에 사용자가 속해 있는 경우
        if (chatRoom.getUsers().contains(requestingUser)) {
            throw new AlreadyInChatRoomException(ExceptionMessage.ALREADY_IN_CHATROOM.getMessage());
        }

        // 현재 인원이 matchCount와 일치하거나 초과하는지 확인
        if (chatRoom.getUsers().size() >= post.getMatchCount()) {
            throw new AlreadyFullChatRoomException(ExceptionMessage.ALREADY_FULL_CHATROOM.getMessage());
        }

        User hostUser = chatRoom.getHostUser();

        // DB에 저장
        ChatNotification chatNotificationEntity = ChatNotification.builder()
                .message(requestingUser.getUsername() + "님이 채팅방 입장을 요청하였습니다.")
                .chatRoom(chatRoom)
                .user(hostUser)
                .requestUser(requestingUser)
                .build();
        chatNotificationRepository.save(chatNotificationEntity);

        // 알림 전송
        chatNotificationService.sendJoinRequestNotification(hostUser, chatRoom, requestingUser);
    }

    // 입장 승인
    @Transactional
    public void approveJoinRequest(Long chatRoomId, Long notificationId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        ChatNotification chatNotification = chatNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ChatNotificationNotFoundException(ExceptionMessage.CHAT_NOTIFICATION_NOT_FOUND.getMessage()));

        User requestUser = chatNotification.getRequestUser();

        // 요청자가 채팅방에 없을 떄 추가
        if (!chatRoom.getUsers().contains(requestUser)) {
            chatRoom.addUser(requestUser);
            chatRoomRepository.save(chatRoom);

            // 입장 메시지 최초 전송
            ChatMessageDTO enterMessage = ChatMessageDTO.builder()
                    .chatRoomId(chatRoomId)
                    .username(requestUser.getUsername())
                    .message(requestUser.getUsername() + "님이 입장하셨습니다.")
                    .type(ChatMessageDTO.MessageType.ENTER)
                    .build();
            messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId, enterMessage);
        }

        // 사용자에게 채팅방 입장이 승인되었음을 알림
        chatNotificationService.sendJoinApprovalNotification(requestUser, chatRoom, notificationId);
    }

    // 사용자가 채팅방에서 퇴장할 때 처리
    @Transactional
    public void leaveChatRoom(Long chatRoomId, User leavingUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        if (chatRoom.getUsers().contains(leavingUser)) {
            // host는 2명 이상일 때 나갈 수 없음
            if(chatRoom.getHostUser().equals(leavingUser) && chatRoom.getUsers().size() >= 2 && chatRoom.getPost() != null) {
                throw new HostExitException(ExceptionMessage.HOST_EXIT.getMessage());
            }
            chatRoom.removeUser(leavingUser);   // 2명 이상일 때 host가 아니면 퇴장

            // 채팅방 인원이 0명일 경우 post와 채팅방 삭제
            if (chatRoom.getUsers().isEmpty()) {
                chatRoomRepository.deleteById(chatRoomId);
            }
            else {
                chatRoomRepository.save(chatRoom);
            }

            // 완전히 나간 경우 퇴장 메시지 전송
            ChatMessageDTO leaveMessage = ChatMessageDTO.builder()
                    .chatRoomId(chatRoomId)
                    .username(leavingUser.getUsername())
                    .message(leavingUser.getUsername() + "님이 퇴장하셨습니다.")
                    .type(ChatMessageDTO.MessageType.LEAVE)
                    .build();
            messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId, leaveMessage);
        }
        else {
            throw new AlreadyOutOfChatRoomException("채팅방에 속해 있지 않습니다.");
        }
    }

    // 강퇴
    @Transactional
    public void kickUserFromChatRoom(Long chatRoomId, Long kickedUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));
        User kickedUser = userRepository.findById(kickedUserId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 호스트가 아니면 강퇴할 수 없음
        if (!chatRoom.getHostUser().getUsername().equals((userService.getAuthenticatedUser().get().getUsername()))) {
            throw new KickException("호스트만 강퇴할 수 있습니다.");
        }

        // 유저 강퇴
        chatRoom.removeUser(kickedUser);
        chatRoomRepository.save(chatRoom);
    }

    // 요청 거절
    @Transactional
    public void rejectJoinRequest(Long chatRoomId, Long notificationId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        ChatNotification chatNotification = chatNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ChatNotificationNotFoundException(ExceptionMessage.CHAT_NOTIFICATION_NOT_FOUND.getMessage()));

        User requestUser = chatNotification.getRequestUser();

        // 요청자에게 거절 알림 전송
        chatNotificationService.sendJoinRejectionNotification(requestUser, chatRoom, notificationId);
    }


    // 채팅방 메시지 가져오기
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessageDtosByChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));
        List<ChatMessage> messages = chatMessageRepository.findByChatRoom(chatRoom);
        return messages.stream()
                .map(this::convertToChatMessageDTO)
                .collect(Collectors.toList());
    }

    // 메시지 전송
    @Transactional
    public void sendMessage(Long chatRoomId, String sendername, String message) {
        User user = userRepository.findByUsername(sendername)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .message(message)
                .build();

        chatMessageRepository.save(chatMessage);
    }

    // 1:1 채팅방 생성
    @Transactional
    public ChatRoom createOneToOneChatRoom(Long receiverId) {
        User requestingUser = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 이미 요청자와 수신자 사이의 1:1 채팅방이 존재하는지 확인
        List<ChatRoom> existingRooms = chatRoomRepository.findByUsersContainsAndUsersContainsAndPostIsNull(requestingUser, receiver);

        if (!existingRooms.isEmpty()) {
            throw new AlreadyInChatRoomException(ExceptionMessage.ALREADY_IN_CHATROOM.getMessage());
        }

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName("1:1 채팅: " + requestingUser.getUsername() + ", " + receiver.getUsername())
                .hostUser(requestingUser)
                .build();

        chatRoom.addUser(requestingUser); // 요청자 추가
        chatRoom.addUser(receiver); // 수신자 추가

        return chatRoomRepository.save(chatRoom);
    }

    private ChatRoomDTO convertToChatRoomDTO(ChatRoom chatRoom) {
        List<String> messageTime = getMessageTime(chatRoom);

        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .hostUserId(chatRoom.getHostUser().getId())
                .userCount(chatRoom.getUsers().size())
                .timeSince(messageTime != null ? messageTime.get(0) : "empty")
                .diffTime(messageTime != null ? Integer.parseInt(messageTime.get(1)) : 0)
                .build();
    }

    private static List<String> getMessageTime(ChatRoom chatRoom) {
        if(chatRoom.getMessages().isEmpty())    return null;

        LocalDateTime dateTime = chatRoom.getMessages().get(chatRoom.getMessages().size()-1).getCreatedAt().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        String timeSince = "";
        int diffTime = 0;

        // dateTime 값이 현재 시각과 정확히 일치하는 경우
        if (dateTime.isEqual(now)) {
            timeSince = "now";
        }
        // dateTime 값이 현재 날짜보다 하루 이상 전인 경우
        else if (dateTime.toLocalDate().isBefore(now.toLocalDate())) {
            diffTime = (int) ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());
            timeSince = "day";
        }
        // dateTime 값이 현재 시간보다 1~24시간 후인 경우
        else if (dateTime.isAfter(now) && dateTime.isBefore(now.plusDays(1)) && dateTime.minusHours(1).isBefore(now)) {
            diffTime = (int) ChronoUnit.HOURS.between(now, dateTime);
            timeSince = "hour";
        }
        // dateTime 값이 현재 시간보다 1시간 이내인 경우
        else if (dateTime.isBefore(now) && dateTime.isAfter(now.minusHours(1))) {
            diffTime = (int) ChronoUnit.MINUTES.between(dateTime, now);
            timeSince = "minute";
        }

        List<String> result = new ArrayList<>();
        result.add(timeSince);
        result.add(String.valueOf(diffTime));
        return result;
    }

    private ChatMessageDTO convertToChatMessageDTO(ChatMessage chatMessage) {
        return ChatMessageDTO.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .username(chatMessage.getSender().getUsername())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .type(ChatMessageDTO.MessageType.CHAT)
                .profileImage(chatMessage.getSender().getProfileImage())
                .build();
    }
}