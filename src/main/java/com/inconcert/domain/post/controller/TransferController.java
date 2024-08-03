package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.TransferService;
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
public class TransferController {
    private final TransferService transferService;
    private final HomeService homeService;

    @GetMapping("/transfer")
    public String transfer(Model model) {
        model.addAttribute(homeService.getAllCategoryPosts("Transfer"));
        return "board/board";
    }

    @GetMapping("/transfer/{postCategoryTitle}")
    public String transferDetail(@PathVariable("postCategoryTitle") String postCategoryTitle, Model model) {
        List<PostDto> postDtos = transferService.getAllTransferPostsByPostCategory(postCategoryTitle);
        model.addAttribute("postDtos", postDtos);
        return "board/board-detail";
    }

    @PostMapping("/transfer/write")
    public void write(@ModelAttribute PostDto postDto) {
        transferService.save(postDto);
    }
}
