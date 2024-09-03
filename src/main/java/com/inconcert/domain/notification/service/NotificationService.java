package com.inconcert.domain.notification.service;

import com.inconcert.domain.notification.dto.NotificationDTO;
import com.inconcert.domain.notification.entity.Notification;
import com.inconcert.domain.notification.repository.NotificationRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.KeywordNotFoundException;
import com.inconcert.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationSseEmitters sseEmitters;
    private final UserService userService;

    @Transactional(readOnly = true)
    public SseEmitter createSseEmitter() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        return sseEmitters.createForUser(user.getId());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Set<String>> getCurrentKeywords() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        String key = "keywords:" + user.getId();
        return ResponseEntity.ok(redisTemplate.opsForSet().members(key));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<String> addKeyword(String keyword) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        String key = "keywords:" + user.getId();
        redisTemplate.opsForSet().add(key, keyword);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);

        return ResponseEntity.ok("키워드 등록 완료");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<String> removeKeyword(String keyword) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        String key = "keywords:" + user.getId();
        redisTemplate.opsForSet().remove(key, keyword);

        return ResponseEntity.ok("키워드가 성공적으로 제거되었습니다.");
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
        User postOwner = post.getUser();

        Notification notification = Notification.builder()
                .message(message)
                .isRead(false)
                .user(postOwner)
                .post(post)
                .type("comment")
                .build();
        notificationRepository.save(notification);
        sseEmitters.sendToUser(postOwner.getId(), convertToDTO(notification));
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

    // 알림 읽음 처리
    @Transactional
    public ResponseEntity<String> markAsRead(Long id) {
        notificationRepository.markAsRead(id);

        return ResponseEntity.ok("알림을 읽었습니다.");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        return ResponseEntity.ok(notificationRepository.findByUserOrderByIsReadAscCreatedAtDesc(user.getId()));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<NotificationDTO>> getNotificationsByTypeAndUser(String type) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        return ResponseEntity.ok(notificationRepository.findByTypeAndUserIdOrderByIsReadAscCreatedAtDesc(type, user.getId()));
    }

    // 알림 삭제
    @Transactional
    public ResponseEntity<String> deleteNotification(Long id){
        notificationRepository.deleteById(id);

        return ResponseEntity.ok("알림이 삭제되었습니다.");
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