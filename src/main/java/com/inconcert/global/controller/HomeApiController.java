package com.inconcert.global.controller;


import com.inconcert.domain.crawling.service.PerformanceService;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.global.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HomeApiController {
    private final HomeService homeService;
    private final PerformanceService performanceService;

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
}