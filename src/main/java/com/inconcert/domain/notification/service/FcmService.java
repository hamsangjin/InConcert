package com.inconcert.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.inconcert.domain.notification.entity.UserToken;
import com.inconcert.domain.notification.repository.MessageRepository;
import com.inconcert.domain.notification.repository.UserTokenRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FcmService {
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final MessageRepository messageRepository;

    public void sendNotification(String token, String title, String body, User user, Post post) {
        System.out.println("sendNotification 호출");
        try {

            // Notification 객체를 생성해 제목과 내용을 설정
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Message 객체를 생성해 대상 토큰과 알림을 설정
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .build();

            // FirebaseMessaging 인스턴스를 사용해 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);

        } catch (Exception e) {
            System.err.println("Error sending FCM message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void notifyKeywordSubscribers(Post post) {
        System.out.println("notifyKeywordSubscribers 호출");

        List<User> users = userRepository.findAll();
        for (User user : users) {

            // 본인 게시물인 경우 알림 호출 X
            if(user.getId() == post.getUser().getId())  continue;

            Set<String> matchedKeywords = user.getKeywords().stream()
                    .filter(keyword -> post.getTitle().contains(keyword) || post.getContent().contains(keyword))
                    .collect(Collectors.toSet());

            System.out.println("matchedKeywords의 길이:" + matchedKeywords.size());

            if (!matchedKeywords.isEmpty()) {
                String token = getTokenByUserId(user.getId());
                if (token != null) {
                    String keywordsStr = String.join(", ", matchedKeywords);
                    String messageBody = post.getTitle();
                    sendNotification(token, String.format("[키워드 알림 - %s]", keywordsStr), messageBody, user, post);
                    saveMessage(String.format("[키워드 알림 - %s]", keywordsStr), messageBody, user, post);
                }
            }
        }
    }

    public String getTokenByUserId(Long userId) {
        System.out.println("getTokenByUserId 호출");
        UserToken userToken = userTokenRepository.findById(userId).orElse(null);
        return userToken != null ? userToken.getToken() : null;
    }

    private void saveMessage(String title, String body, User user, Post post) {
        System.out.println("saveMessage 호출");
        com.inconcert.domain.notification.entity.Message message = new com.inconcert.domain.notification.entity.Message(title, body, user, post);
        messageRepository.save(message);
    }
}