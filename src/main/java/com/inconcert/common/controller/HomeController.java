package com.inconcert.common.controller;

import com.inconcert.domain.scraping.service.PerformanceService;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.common.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final HomeService homeService;
    private final PerformanceService performanceService;

    @GetMapping
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        List<PostDTO> popularPosts = homeService.getPopularPosts();

        // 기존의 게시글 로드
        List<PostDTO> infoPosts = homeService.getAllPostDTOsByCategoryTitle("info");
        List<PostDTO> reviewPosts = homeService.getAllPostDTOsByCategoryTitle("review");
        List<PostDTO> matchPosts = homeService.getAllPostDTOsByCategoryTitle("match");
        List<PostDTO> transferPosts = homeService.getAllPostDTOsByCategoryTitle("transfer");

        model.addAttribute("popularPosts", popularPosts);
        model.addAttribute("infoPosts", infoPosts);
        model.addAttribute("reviewPosts", reviewPosts);
        model.addAttribute("matchPosts", matchPosts);
        model.addAttribute("transferPosts", transferPosts);
        model.addAttribute("isCrawling", performanceService.isCrawling());
        return "home";
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