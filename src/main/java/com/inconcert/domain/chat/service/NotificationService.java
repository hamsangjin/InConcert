package com.inconcert.domain.chat.service;

import com.inconcert.domain.chat.dto.NotificationMessage;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.user.entity.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 호스트에게 채팅방 내부에서 알림을 보내는 메서드
    public void sendJoinRequestNotification(User hostUser, ChatRoom chatRoom, User requestingUser) {
        String destination = "/topic/chat/room/" + chatRoom.getId() + "/requests";
        NotificationMessage notification = new NotificationMessage(requestingUser.getUsername() + "님이 채팅방 입장을 요청했습니다.", chatRoom.getId());
        messagingTemplate.convertAndSend(destination, notification);
    }

    // 사용자가 승인되었을 때 알림을 보내는 메서드
    public void sendJoinApprovalNotification(User user, ChatRoom chatRoom) {
        String destination = "/topic/notifications/" + user.getId();
        NotificationMessage notification = new NotificationMessage("채팅방 입장이 승인되었습니다.", chatRoom.getId());
        messagingTemplate.convertAndSend(destination, notification);
    }
}
