package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.EditService;
import com.inconcert.domain.post.service.MatchService;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;
    private final UserService userService;
    private final EditService editService;

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

    @PostMapping("/write")
    public String write(@ModelAttribute PostDto postDto) {
        matchService.save(postDto);
        return "redirect:/match";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/delete")
    public String deletePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId) {
        matchService.deletePost(postId);
        return "redirect:/info/" + postCategoryTitle;
    }


    @GetMapping("/{postCategoryTitle}/{postId}/edit")
    public String editPost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                           @PathVariable("postId") Long postId, Model model) {
        PostDto postDto = matchService.getPostById(postId);
        model.addAttribute("post", postDto);
        model.addAttribute("categoryTitle", "match");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/editform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/edit")
    public String updatePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId,
                             @ModelAttribute PostDto postDto,
                             @RequestParam("newCategoryTitle") String newCategoryTitle,
                             @RequestParam("newPostCategoryTitle") String newPostCategoryTitle) {
        Long updatedPostId = editService.updatePost(postId, postDto, "match", newCategoryTitle, newPostCategoryTitle);
        return "redirect:/" + newCategoryTitle + '/' + newPostCategoryTitle + '/' + updatedPostId;
    }
}
