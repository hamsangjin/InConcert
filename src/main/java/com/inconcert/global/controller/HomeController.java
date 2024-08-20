package com.inconcert.global.controller;

import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.global.service.CrawlingService;
import com.inconcert.global.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;
    private final CrawlingService crawlingService;

    @GetMapping("/home")
    public String home(Model model) {
        // 크롤링 판단
        crawlingService.crawlIfNecessary();

        // 기존의 게시글 로드
        List<PostDTO> infoPosts = homeService.getAllCategoryPosts("info");
        List<PostDTO> reviewPosts = homeService.getAllCategoryPosts("review");
        List<PostDTO> matchPosts = homeService.getAllCategoryPosts("match");
        List<PostDTO> transferPosts = homeService.getAllCategoryPosts("transfer");
        List<PostDTO> popularPosts = homeService.findLatestPostsByPostCategory();

        model.addAttribute("infoPosts", infoPosts);
        model.addAttribute("reviewPosts", reviewPosts);
        model.addAttribute("matchPosts", matchPosts);
        model.addAttribute("transferPosts", transferPosts);
        model.addAttribute("popularPosts", popularPosts);

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