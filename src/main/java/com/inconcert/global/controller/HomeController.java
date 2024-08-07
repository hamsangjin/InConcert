package com.inconcert.global.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.service.CrawlingService;
import com.inconcert.global.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;
    private final UserService userService;
    private final CrawlingService crawlingService;

    @GetMapping("/home")
    public String home(Model model) {
        // 크롤링 판단
        crawlingService.crawlIfNecessary();

        // 기존의 게시글 로드
        List<PostDto> infoPosts = homeService.getAllCategoryPosts("info");
        List<PostDto> reviewPosts = homeService.getAllCategoryPosts("review");
        List<PostDto> matchPosts = homeService.getAllCategoryPosts("match");
        List<PostDto> transferPosts = homeService.getAllCategoryPosts("transfer");
        List<PostDto> popularPosts = homeService.findLatestPostsByPostCategory();

        model.addAttribute("infoPosts", infoPosts);
        model.addAttribute("reviewPosts", reviewPosts);
        model.addAttribute("matchPosts", matchPosts);
        model.addAttribute("transferPosts", transferPosts);
        model.addAttribute("popularPosts", popularPosts);

        return "home";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username"));
        model.addAttribute("user", user);
        return "/user/mypage";
    }

    @GetMapping("/write")
    public String write(Model model) {
        model.addAttribute("postDto", new PostDto());
        return "board/writeform";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("posts", homeService.findByKeyword(keyword));
        model.addAttribute("headerKeyword", keyword);
        return "search-result";
    }
}