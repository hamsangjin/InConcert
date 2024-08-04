package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @GetMapping
    public String transfer(Model model) {
        model.addAttribute("Musicalposts", transferService.getAllTransferPostsByPostCategory("musical"));
        model.addAttribute("Concertposts", transferService.getAllTransferPostsByPostCategory("concert"));
        model.addAttribute("Theaterposts", transferService.getAllTransferPostsByPostCategory("theater"));
        model.addAttribute("Etcposts", transferService.getAllTransferPostsByPostCategory("etc"));
        model.addAttribute("categoryTitle", "transfer");
        return "board/board";
    }

    @GetMapping("/{postCategoryTitle}")
    public String transferDetail(@PathVariable("postCategoryTitle") String postCategoryTitle, Model model) {
        model.addAttribute("posts", transferService.getAllTransferPostsByPostCategory(postCategoryTitle));
        model.addAttribute("categoryTitle", "transfer");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/board-detail";
    }

    @GetMapping("/{postCategoryTitle}/{postId}")
    public String getPostDetail(@PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId, Model model) {
        model.addAttribute("post", transferService.getPostById(postId));
        model.addAttribute("categoryTitle", "transfer");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/post-detail";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDto postDto) {
        transferService.save(postDto);
        return "redirect:/transfer";
    }
}
