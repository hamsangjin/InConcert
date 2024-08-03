package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.InfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class InfoController {
    private final InfoService infoService;

    @GetMapping("/info")
    public String info(Model model) {
        model.addAttribute("Musicalposts", infoService.getAllInfoPostsByPostCategory("Musical"));
        model.addAttribute("Concertposts", infoService.getAllInfoPostsByPostCategory("Concert"));
        model.addAttribute("Theaterposts", infoService.getAllInfoPostsByPostCategory("Theater"));
        model.addAttribute("Etcposts", infoService.getAllInfoPostsByPostCategory("Etc"));
        return "board/board";
    }

    @GetMapping("/info/{postCategoryTitle}")
    public String infoDetail(@PathVariable("postCategoryTitle") String postCategoryTitle, Model model) {
        List<PostDto> postDtos = infoService.getAllInfoPostsByPostCategory(postCategoryTitle);
        model.addAttribute("postDtos", postDtos);
        return "board/board-detail";
    }

    @PostMapping("/info/write")
    public String write(@ModelAttribute PostDto postDto, @RequestParam("categoryTitle") String categoryTitle){
        System.out.println(categoryTitle);
        System.out.println(postDto.getCategoryTitle());
        System.out.println(postDto);
        infoService.save(postDto);
        return "redirect:/info";
    }
}
