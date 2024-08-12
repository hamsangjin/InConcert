package com.inconcert.domain.notification.service;

import com.inconcert.domain.notification.entity.Notification;
import com.inconcert.domain.notification.repository.NotificationRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseEmitters sseEmitters;

    public void setKeyword(Long userId, String keyword) {
        String key = "keywords:" + userId;
        redisTemplate.opsForSet().add(key, keyword);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    public Set<String> getCurrentKeywords(Long userId) {
        String key = "keywords:" + userId;
        return redisTemplate.opsForSet().members(key);
    }

    public void removeKeyword(Long userId, String keyword) {
        String key = "keywords:" + userId;
        redisTemplate.opsForSet().remove(key, keyword);
    }

    //  Redis 에 저장된 키워드를 가진 사용자만을 대상으로 알림을 생성
    public void publishNotification(String title, User author) {
        try {
            Set<String> keys = redisTemplate.keys("keywords:*");
            for (String key : keys) {
                Long userId = Long.parseLong(key.split(":")[1]);
                if (userId.equals(author.getId())) continue; // 자신의 게시글은 알림 안 보냄

                Set<String> keywords = redisTemplate.opsForSet().members(key);
                for (String keyword : keywords) {
                    if (title.contains(keyword)) {
                        User user = userRepository.findById(userId).orElse(null);
                        if (user != null) {
                            String message = "[키워드 알림] " + title;
                            Notification notification = Notification.builder()
                                    .keyword(keyword)
                                    .message(message)
                                    .isRead(false)
                                    .user(user)
                                    .build();
                            notificationRepository.save(notification);
                            sseEmitters.sendToUser(userId, message);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in publishNotification", e);
        }
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    public SseEmitter createSseEmitter(Long userId) {
        return sseEmitters.createForUser(userId);
    }
}
