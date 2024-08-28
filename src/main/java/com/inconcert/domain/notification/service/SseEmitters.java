package com.inconcert.domain.notification.service;

import com.inconcert.domain.notification.dto.NotificationDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitters {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createForUser(Long userId) {
        SseEmitter emitter = new SseEmitter(1800000L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        // 연결 직후 테스트 이벤트 전송
        try {
            emitter.send(SseEmitter.event().name("connect").data("Connected successfully"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    public void sendToUser(Long userId, NotificationDTO notification) {
        SseEmitter emitter = this.emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
            } catch (IOException e) {
                this.emitters.remove(userId);
            }
        }
    }
}
