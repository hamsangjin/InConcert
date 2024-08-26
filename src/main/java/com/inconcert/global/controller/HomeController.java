package com.inconcert.global.controller;

import com.inconcert.domain.crawling.service.CrawlingSseEmitters;
import com.inconcert.domain.crawling.service.PerformanceService;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.global.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final HomeService homeService;
    private final PerformanceService performanceService;
    private final CrawlingSseEmitters crawlingSseEmitters;

    @GetMapping("/home")
    public String home(Model model) {

        // 기존의 게시글 로드
        List<PostDTO> reviewPosts = homeService.getAllCategoryPosts("review");
        List<PostDTO> matchPosts = homeService.getAllCategoryPosts("match");
        List<PostDTO> transferPosts = homeService.getAllCategoryPosts("transfer");

        model.addAttribute("reviewPosts", reviewPosts);
        model.addAttribute("matchPosts", matchPosts);
        model.addAttribute("transferPosts", transferPosts);


        // 비동기로 크롤링 시작
        log.info("Starting asynchronous crawling");
        CompletableFuture<Void> crawlingFuture = performanceService.startCrawlingAsync();
        crawlingFuture.thenRun(() -> log.info("Crawling completed"));

        return "home";
    }

    @GetMapping("/api/crawling/progress")
    public SseEmitter streamCrawlingProgress() {
        log.info("Received request for SSE connection");
        SseEmitter emitter = crawlingSseEmitters.create();
        log.info("Created new SSE emitter: {}", emitter);

        emitter.onCompletion(() -> log.info("SSE connection completed"));
        emitter.onTimeout(() -> log.info("SSE connection timed out"));

        try {
            emitter.send(SseEmitter.event()
                    .name("test")
                    .data("Test SSE connection")
            );
            log.info("Sent test SSE event");
        } catch (IOException e) {
            log.error("Error sending test SSE event", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "keyword", required = false) String keyword,
                         @RequestParam(name = "page", defaultValue = "0") int page,
                         @RequestParam(name = "size", defaultValue = "10") int size,
                         Model model) {

        Page<PostDTO> postPage = homeService.getPostDTOsByKeyword(keyword, page, size);

        model.addAttribute("postsPage", postPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("headerKeyword", keyword);

        return "search-result";
    }

    @GetMapping("/write")
    public String write(Model model) {
        model.addAttribute("postDto", new PostDTO());
        return "board/writeform";
    }
}