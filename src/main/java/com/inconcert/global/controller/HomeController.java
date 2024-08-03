package com.inconcert.global.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.global.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    @GetMapping("/home")
    public String home(Model model) {
        List<PostDto> infoPosts = homeService.getAllCategoryPosts("Info");
        List<PostDto> reviewPosts = homeService.getAllCategoryPosts("Review");
        List<PostDto> matchPosts = homeService.getAllCategoryPosts("Match");
        List<PostDto> transferPosts = homeService.getAllCategoryPosts("Transfer");

        model.addAttribute("infoPosts", infoPosts);
        model.addAttribute("reviewPosts", reviewPosts);
        model.addAttribute("matchPosts", matchPosts);
        model.addAttribute("transferPosts", transferPosts);
        return "home";
    }

    @GetMapping("/write")
    public String write(Model model) {
        model.addAttribute("postDto", new PostDto());
        return "board/writeform";
    }
}