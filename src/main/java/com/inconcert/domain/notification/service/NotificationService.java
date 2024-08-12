package com.inconcert.domain.notification.service;

import com.inconcert.domain.notification.dto.NotificationDto;
import com.inconcert.domain.notification.entity.Notification;
import com.inconcert.domain.notification.repository.NotificationRepository;
import com.inconcert.domain.post.entity.Post;
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
    public void publishNotification(Post post) {
        try {
            Set<String> keys = redisTemplate.keys("keywords:*");
            for (String key : keys) {
                Long userId = Long.parseLong(key.split(":")[1]);
                if (userId.equals(post.getUser().getId())) continue;

                Set<String> keywords = redisTemplate.opsForSet().members(key);
                for (String keyword : keywords) {
                    if (post.getTitle().contains(keyword)) {
                        User user = userRepository.findById(userId).orElse(null);
                        if (user != null) {
                            String message = "[키워드 알림] " + post.getTitle();
                            Notification notification = Notification.builder()
                                    .keyword(keyword)
                                    .message(message)
                                    .isRead(false)
                                    .user(user)
                                    .post(post)
                                    .build();
                            notificationRepository.save(notification);
                            sseEmitters.sendToUser(userId, convertToDto(notification));
                            System.out.println("===================" + post.getPostCategory());
                            System.out.println("===================" + post.getPostCategory().getCategory());
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

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found")); // 예외 처리 하기
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public NotificationDto convertToDto(Notification notification) {
        Post post = notification.getPost();
        return new NotificationDto(
                notification.getId(),
                notification.getKeyword(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                post != null ? post.getPostCategory().getCategory().getTitle() : null,
                post != null ? post.getPostCategory().getTitle() : null,
                post != null ? post.getId() : null
        );
    }

    public List<Notification> getAllNotifications(User user) {
        return notificationRepository.findByUser(user);
    }

}
