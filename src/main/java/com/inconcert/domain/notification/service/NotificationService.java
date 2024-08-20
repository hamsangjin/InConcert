package com.inconcert.domain.notification.service;

import com.inconcert.domain.notification.dto.NotificationDTO;
import com.inconcert.domain.notification.entity.Notification;
import com.inconcert.domain.notification.repository.NotificationRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.KeywordNotFoundException;
import com.inconcert.global.exception.NotificationNotFoundException;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseEmitters sseEmitters;
    private final UserService userService;

    @Transactional(readOnly = true)
    public SseEmitter createSseEmitter() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        return sseEmitters.createForUser(user.getId());
    }

    @Transactional(readOnly = true)
    public Set<String> getCurrentKeywords() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        String key = "keywords:" + user.getId();
        return redisTemplate.opsForSet().members(key);
    }

    @Transactional(readOnly = true)
    public void addKeyword(String keyword) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        String key = "keywords:" + user.getId();
        redisTemplate.opsForSet().add(key, keyword);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    @Transactional(readOnly = true)
    public void removeKeyword(String keyword) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        String key = "keywords:" + user.getId();
        redisTemplate.opsForSet().remove(key, keyword);
    }

    // Redis 에 저장된 키워드를 가진 사용자만을 대상으로 알림을 생성
    @Transactional
    public void createKeywordsNotification(Post post) {
        Set<String> keys = redisTemplate.keys("keywords:*");
        try{
            for (String key : keys) {
                Long userId = Long.parseLong(key.split(":")[1]);
                if (userId.equals(post.getUser().getId())) continue;

                Set<String> keywords = redisTemplate.opsForSet().members(key);
                Set<String> matchedKeywords = new HashSet<>();

                for (String keyword : keywords) {
                    if (post.getTitle().contains(keyword)) {
                        matchedKeywords.add(keyword);
                    }
                }

                if (!matchedKeywords.isEmpty()) {
                    Optional<User> user = userRepository.findById(userId);
                    if (user.isPresent()) {
                        String keywordString = String.join(", ", matchedKeywords);
                        String message = "[키워드 알림] " + post.getTitle();
                        Notification notification = Notification.builder()
                                .keyword(keywordString)
                                .message(message)
                                .isRead(false)
                                .type("keyword")
                                .user(user.get())
                                .post(post)
                                .build();
                        notificationRepository.save(notification);
                        sseEmitters.sendToUser(userId, convertToDTO(notification));
                    }
                }
            }
        }catch (NullPointerException e){
            throw new KeywordNotFoundException(ExceptionMessage.KEYWORD_NOT_FOUND.getMessage());
        }

    }

    // 댓글 알림 생성
    @Transactional
    public void createCommentsNotification(Post post, String content) {
        String message = "[댓글 알림] " + content;
        User user = userRepository.findById(post.getUser().getId()).get();

        Notification notification = Notification.builder()
                .message(message)
                .isRead(false)
                .user(user)
                .post(post)
                .type("comment")
                .build();
        notificationRepository.save(notification);
        sseEmitters.sendToUser(user.getId(), convertToDTO(notification));
    }

    // 좋아요 알림 생성
    @Transactional
    public void createLikesNotification(Post post, User user, boolean liked) {
        User postOwner = post.getUser();

        if(liked){
            String message = "[좋아요 알림] " + user.getNickname() + "님이 좋아요를 눌렀습니다.";

            Notification notification = Notification.builder()
                    .message(message)
                    .isRead(false)
                    .user(postOwner)
                    .post(post)
                    .type("likes")
                    .build();
            notificationRepository.save(notification);
            sseEmitters.sendToUser(postOwner.getId(), convertToDTO(notification));
        } else{
            notificationRepository.deleteByTypeAndUserIdAndPostId(postOwner.getId(), post.getId());
        }
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(ExceptionMessage.NOTIFICATION_NOT_FOUND.getMessage()));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        List<Notification> notifications = notificationRepository.findByUserOrderByIsReadAscCreatedAtDesc(user);

        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByTypeAndUser(String type) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        List<Notification> notifications = notificationRepository.findByTypeAndUserIdOrderByIsReadAscCreatedAtDesc(type, user.getId());

        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 알림 삭제 로직
    @Transactional
    public void deleteNotification(Long id){
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(ExceptionMessage.NOTIFICATION_NOT_FOUND.getMessage()));

        notificationRepository.delete(notification);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        Post post = notification.getPost();
        return new NotificationDTO(
                notification.getId(),
                notification.getKeyword(),
                notification.getMessage(),
                notification.isRead(),
                notification.getType(),
                notification.getCreatedAt(),
                post != null ? post.getPostCategory().getCategory().getTitle() : null,
                post != null ? post.getPostCategory().getTitle() : null,
                post != null ? post.getId() : null
        );
    }
}