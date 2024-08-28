package com.inconcert.domain.crawling.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inconcert.domain.post.dto.PostDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
@RequiredArgsConstructor
public class CrawlingSseEmitters {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public SseEmitter create() {
        SseEmitter emitter = new SseEmitter(1800000L);
        this.emitters.add(emitter);
        emitter.onCompletion(() -> {
            this.emitters.remove(emitter);
            log.info("SSE connection completed");
        });
        emitter.onTimeout(() -> {
            this.emitters.remove(emitter);
            log.info("SSE connection timed out");
        });

        try {
            emitter.send(SseEmitter.event().name("connect").data("Crawling connection established"));
            log.info("SSE connection established");
        } catch (IOException e) {
            log.error("Error sending initial SSE message", e);
            this.emitters.remove(emitter);
        }

        return emitter;
    }

    // sse 에 스크래핑 시작/완료 메시지 전송
    public void sendStatusUpdate(String status, String message) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                String jsonStatus = objectMapper.writeValueAsString(Map.of("status", status, "message", message));
                emitter.send(SseEmitter.event().name("crawlingStatusUpdate").data(jsonStatus, MediaType.APPLICATION_JSON));
                log.info("Successfully sent status update: {}", status);
            } catch (Exception e) {
                log.error("Failed to send SSE status update", e);
                deadEmitters.add(emitter);
            }
        });

        emitters.removeAll(deadEmitters);
        log.info("Removed {} dead emitters", deadEmitters.size());
    }

    // postDTO 를 json 형태로 sse 전송
    public void sendBatchUpdate(List<PostDTO> postDTOs) {
        log.info("Current number of active SSE connections: {}", emitters.size());
        log.info("Sending batch update with {} posts", postDTOs.size());
        List<SseEmitter> deadEmitters = new ArrayList<>();

        try {
            String jsonPostDTOs = objectMapper.writeValueAsString(postDTOs);
            log.info("Successfully serialized postDTOs");
            log.debug("Serialized JSON: {}", jsonPostDTOs);

            emitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event().name("crawlingBatchUpdate").data(jsonPostDTOs, MediaType.APPLICATION_JSON));
                    log.info("Successfully sent batch update to an emitter");
                } catch (IOException e) {
                    log.error("Failed to send SSE event", e);
                    deadEmitters.add(emitter);
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize postDTOs", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while processing batch update", e);
        }

        emitters.removeAll(deadEmitters);
        log.info("Removed {} dead emitters after batch update", deadEmitters.size());
    }
}
