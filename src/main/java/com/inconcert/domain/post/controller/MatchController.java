package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.MatchService;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;
    private final UserService userService;

    @GetMapping
    public String match(Model model) {
        model.addAttribute("Musicalposts", matchService.getAllMatchPostsByPostCategory("musical"));
        model.addAttribute("Concertposts", matchService.getAllMatchPostsByPostCategory("concert"));
        model.addAttribute("Theaterposts", matchService.getAllMatchPostsByPostCategory("theater"));
        model.addAttribute("Etcposts", matchService.getAllMatchPostsByPostCategory("etc"));
        model.addAttribute("categoryTitle", "match");
        return "board/board";
    }

    @GetMapping("/{postCategoryTitle}")
    public String matchDetail(@PathVariable("postCategoryTitle") String postCategoryTitle, Model model) {
        model.addAttribute("posts", matchService.getAllMatchPostsByPostCategory(postCategoryTitle));
        model.addAttribute("categoryTitle", "match");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/board-detail";
    }

    @GetMapping("/{postCategoryTitle}/{postId}")
    public String getPostDetail(@PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId, Model model) {
        model.addAttribute("post", matchService.getPostById(postId));
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("categoryTitle", "match");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/post-detail";
    }

    @GetMapping("/{postCategoryTitle}/search")
    public String search(@PathVariable("postCategoryTitle") String postCategoryTitle,
                         @RequestParam(name = "keyword") String keyword,
                         @RequestParam(name = "period", required = false, defaultValue = "all") String period,
                         @RequestParam(name = "type", required = false, defaultValue = "title+content") String type,
                         Model model) {
        List<PostDto> searchResults = matchService.findByKeywordAndFilters(postCategoryTitle, keyword, period, type);
        model.addAttribute("posts", searchResults);
        model.addAttribute("categoryTitle", "match");
        return "board/board-detail";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDto postDto) {
        matchService.save(postDto);
        return "redirect:/match";
    }
}
