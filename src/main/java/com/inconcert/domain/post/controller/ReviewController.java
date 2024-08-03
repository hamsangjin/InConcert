package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.ReviewService;
import com.inconcert.global.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final HomeService homeService;

    @GetMapping("/review")
    public String review(Model model) {
        model.addAttribute(homeService.getAllCategoryPosts("Review"));
        return "board/board-detail";
    }

    @PostMapping("/review/write")
    public void write(@ModelAttribute PostDto postDto) {
        reviewService.save(postDto);
    }
}
