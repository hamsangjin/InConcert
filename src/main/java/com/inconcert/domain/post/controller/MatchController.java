package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.MatchService;
import com.inconcert.global.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;
    private final HomeService homeService;

    @GetMapping("/match")
    public String info(Model model) {
        model.addAttribute(homeService.getAllCategoryPosts("Match"));
        return "board/board";
    }

    @GetMapping("/match/{postCategoryTitle}")
    public String matchDetail(@PathVariable("postCategoryTitle") String postCategoryTitle, Model model) {
        List<PostDto> postDtos = matchService.getAllMatchPostsByPostCategory(postCategoryTitle);
        model.addAttribute("postDtos", postDtos);
        return "board/board-detail";
    }

    @PostMapping("/match/write")
    public void write(@ModelAttribute PostDto postDto) {
        matchService.save(postDto);
    }
}
