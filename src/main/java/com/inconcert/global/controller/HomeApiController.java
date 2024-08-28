package com.inconcert.global.controller;


import com.inconcert.domain.crawling.service.CrawlingSseEmitters;
import com.inconcert.domain.crawling.service.PerformanceService;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.global.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HomeApiController {
    private final HomeService homeService;
    private final PerformanceService performanceService;
    private final CrawlingSseEmitters crawlingSseEmitters;

    @GetMapping("/api/posts/latest")
    public ResponseEntity<List<PostDTO>> getLatestPosts() {
        List<PostDTO> latestPosts = homeService.getLatestPosts();
        return ResponseEntity.ok(latestPosts);
    }

    @GetMapping("/api/posts/popular")
    public ResponseEntity<List<PostDTO>> getPopularPosts() {
        List<PostDTO> popularPosts = homeService.getPopularPosts();
        return ResponseEntity.ok(popularPosts);
    }

    @GetMapping("/api/crawling/status")
    public boolean getCrawlingStatus() {
        return performanceService.isCrawling();
    }

    @GetMapping("/api/crawling/progress")
    public SseEmitter streamCrawlingProgress() {
        log.info("Received request for SSE connection");
        SseEmitter emitter = crawlingSseEmitters.create();
        log.info("Created new SSE emitter: {}", emitter);

        // Send a test event immediately after connection
        try {
            emitter.send(SseEmitter.event()
                    .name("test")
                    .data("Test SSE connection")
            );
            log.info("Sent test SSE event");
        } catch (IOException e) {
            log.error("Error sending test SSE event", e);
        }

        return emitter;
    }
}