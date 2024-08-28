package com.inconcert.domain.chat.service;

import com.inconcert.domain.chat.dto.NotificationMessageDTO;
import com.inconcert.domain.chat.entity.ChatNotification;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.chat.repository.ChatNotificationRepository;
import com.inconcert.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatNotificationRepository chatNotificationRepository;

    // 사용자의 채팅 알림 불러오기
    public ResponseEntity<List<NotificationMessageDTO>> getNotificationMessageDTOsByUserId(Long userId) {
        return ResponseEntity.ok(chatNotificationRepository.findByUserId(userId));
    }

    // 승인 요청 보내기
    @Transactional
    public void sendJoinRequestNotification(User hostUser, ChatRoom chatRoom, User requestingUser) {
        // DB에 저장
        ChatNotification chatNotificationEntity = ChatNotification.builder()
                .message(requestingUser.getNickname() + "님이 채팅방 입장을 요청하였습니다.")
                .chatRoom(chatRoom)
                .user(hostUser)
                .requestUser(requestingUser)
                .build();
        chatNotificationRepository.save(chatNotificationEntity);

        // stomp 관련 엔드포인트
        String destination = "/topic/notifications/" + hostUser.getId();

        NotificationMessageDTO notification = NotificationMessageDTO.builder()
                .message(requestingUser.getNickname() + "님이 채팅방 입장을 요청했습니다.")
                .chatRoomId(chatRoom.getId())
                .userId(requestingUser.getId())
                .build();

        messagingTemplate.convertAndSend(destination, notification);
    }

    // 승인하기
    @Transactional
    public void sendJoinApprovalNotification(User user, ChatRoom chatRoom, Long notificationId) {
        // stomp 관련 엔드포인트
        String destination = "/topic/notifications/" + user.getId();

        NotificationMessageDTO notification = NotificationMessageDTO.builder()
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
        ChatNotification chatNotification = ChatNotification.builder()
                .user(user)
                .chatRoom(chatRoom)
                .message("채팅방 '" + chatRoom.getRoomName() + "'의 입장이 거절되었습니다.")
                .build();
        chatNotificationRepository.save(chatNotification);

        String destination = "/topic/notifications/" + user.getId();

        NotificationMessageDTO notification = NotificationMessageDTO.builder()
                .message("채팅방 '" + chatRoom.getRoomName() + "'의 입장이 거절되었습니다.")
                .chatRoomId(chatRoom.getId())
                .userId(user.getId())
                .build();
        messagingTemplate.convertAndSend(destination, notification);

        // 기존의 notificationId로 DB에서 제거
        chatNotificationRepository.deleteById(notificationId);
    }

    // 알림 삭제 로직
    @Transactional
    public ResponseEntity<String> deleteNotification(Long notificationId) {
        chatNotificationRepository.deleteById(notificationId);
        return ResponseEntity.ok("알림이 삭제되었습니다.");
    }
}