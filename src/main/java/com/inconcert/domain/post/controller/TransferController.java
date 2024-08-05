package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.TransferService;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;
    private final UserService userService;

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
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("categoryTitle", "transfer");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/post-detail";
    }

    @GetMapping("/{postCategoryTitle}/search")
    public String search(@PathVariable("postCategoryTitle") String postCategoryTitle,
                         @RequestParam(name = "keyword") String keyword,
                         @RequestParam(name = "period", required = false, defaultValue = "all") String period,
                         @RequestParam(name = "type", required = false, defaultValue = "title+content") String type,
                         Model model) {
        List<PostDto> searchResults = transferService.findByKeywordAndFilters(postCategoryTitle, keyword, period, type);
        model.addAttribute("posts", searchResults);
        model.addAttribute("categoryTitle", "transfer");
        return "board/board-detail";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDto postDto) {
        transferService.save(postDto);
        return "redirect:/transfer";
    }
}
