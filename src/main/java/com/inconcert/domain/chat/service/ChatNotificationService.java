package com.inconcert.domain.chat.service;

import com.inconcert.domain.chat.dto.NotificationMessage;
import com.inconcert.domain.chat.entity.ChatNotification;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.chat.repository.ChatNotificationRepository;
import com.inconcert.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatNotificationRepository chatNotificationRepository;

    // User별 채팅방 찾기
    public List<ChatNotification> findByUserId(Long userId) {
        return chatNotificationRepository.findByUserId(userId);
    }

    // 승인 요청 보내기
    @Transactional
    public void sendJoinRequestNotification(User hostUser, ChatRoom chatRoom, User requestingUser) {
        String destination = "/topic/notifications/" + hostUser.getId();

        NotificationMessage notification = NotificationMessage.builder()
                .id(null)   // ID는 null로 초기화, 필요시 DB에 저장 후 설정 가능
                .message(requestingUser.getUsername() + "님이 채팅방 입장을 요청했습니다.")
                .chatRoomId(chatRoom.getId())
                .userId(requestingUser.getId())
                .build();
        messagingTemplate.convertAndSend(destination, notification);
    }

    // 승인하기
    @Transactional
    public void sendJoinApprovalNotification(User user, ChatRoom chatRoom, Long notificationId) {
        String destination = "/topic/notifications/" + user.getId();

        NotificationMessage notification = NotificationMessage.builder()
                .id(null)
                .message("채팅방 입장이 승인되었습니다.")
                .chatRoomId(chatRoom.getId())
                .userId(user.getId())
                .build();
        messagingTemplate.convertAndSend(destination, notification);

        // 승인 후 채팅 알림 삭제
        chatNotificationRepository.deleteById(notificationId);
    }

    // 요청 거절 알림 전송
    @Transactional
    public void sendJoinRejectionNotification(User user, ChatRoom chatRoom, Long notificationId) {
        // 알림을 먼저 저장한 후에 해당 알림의 ID를 클라이언트로 전달
        ChatNotification chatNotification = ChatNotification.builder()
                .user(user)
                .chatRoom(chatRoom)
                .message("채팅방 '" + chatRoom.getRoomName() + "'의 입장이 거절되었습니다.")
                .build();
        chatNotification = chatNotificationRepository.save(chatNotification); // 저장 후 ID 획득

        String destination = "/topic/notifications/" + user.getId();

        NotificationMessage notification = NotificationMessage.builder()
                .id(chatNotification.getId())  // 실제 저장된 ID를 전송
                .message("채팅방 '" + chatRoom.getRoomName() + "'의 입장이 거절되었습니다.")
                .chatRoomId(chatRoom.getId())
                .userId(user.getId())
                .build();
        messagingTemplate.convertAndSend(destination, notification);

        // 기존의 notificationId로 DB에서 제거
        chatNotificationRepository.deleteById(notificationId);
    }

    // 알림 불러오기
//    @Transactional(readOnly = true)
//    public List<NotificationMessage> getNotificationsByChatRoomId(Long chatRoomId) {
//        List<ChatNotification> chatNotifications = chatNotificationRepository.findByChatRoomId(chatRoomId);
//        return chatNotifications.stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }

//    private NotificationMessage convertToDto(ChatNotification chatNotification) {
//        return NotificationMessage.builder()
//                .message(chatNotification.getMessage())
//                .chatRoomId(chatNotification.getChatRoom().getId())
//                .userId(chatNotification.getUser().getId())
//                .build();
//    }

    // 사용자별로 거절된 알림 불러오기
    public List<NotificationMessage> findRejectedNotificationsByUserId(Long userId) {
        List<ChatNotification> notifications = chatNotificationRepository.findByUserId(userId);
        return notifications.stream()
                .map(NotificationMessage::new)
                .collect(Collectors.toList());
    }

    // 알림 삭제 로직
    @Transactional
    public void deleteNotification(Long notificationId) {
        chatNotificationRepository.deleteById(notificationId);
    }
}