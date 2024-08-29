package com.inconcert.common.controller;

import com.inconcert.domain.scraping.service.ScrapingSseEmitters;
import com.inconcert.domain.scraping.service.PerformanceService;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.common.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HomeApiController {
    private final HomeService homeService;
    private final PerformanceService performanceService;
    private final ScrapingSseEmitters scrapingSseEmitters;

    @GetMapping("/api/posts/latest")
    public ResponseEntity<List<PostDTO>> getLatestPosts() {
        return homeService.getLatestPosts();
    }

    @GetMapping("/api/posts/popular")
    public ResponseEntity<List<PostDTO>> getPopularPosts() {
        return homeService.getPopularPosts();
    }

    @GetMapping("/api/crawling/status")
    public boolean getCrawlingStatus() {
        return performanceService.isCrawling();
    }

    @GetMapping("/api/crawling/progress")
    public SseEmitter streamCrawlingProgress() {
        return scrapingSseEmitters.create();
    }
}