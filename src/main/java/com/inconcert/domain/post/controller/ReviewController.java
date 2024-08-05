package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.EditService;
import com.inconcert.domain.post.service.ReviewService;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final HomeService homeService;
    private final UserService userService;
    private final EditService editService;

    @GetMapping
    public String review(Model model) {
        model.addAttribute("posts", homeService.getAllCategoryPosts("review"));
        model.addAttribute("categoryTitle", "review");
        return "board/board-detail";
    }

    @GetMapping("/{postCategoryTitle}/{postId}")
    public String getPostDetail(@PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId, Model model) {
        model.addAttribute("post", reviewService.getPostById(postId));
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("categoryTitle", "review");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/post-detail";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDto postDto) {
        reviewService.save(postDto);
        return "redirect:/review";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/delete")
    public String deletePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId) {
        reviewService.deletePost(postId);
        return "redirect:/info/" + postCategoryTitle;
    }


    @GetMapping("/{postCategoryTitle}/{postId}/edit")
    public String editPost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                           @PathVariable("postId") Long postId, Model model) {
        PostDto postDto = reviewService.getPostById(postId);
        model.addAttribute("post", postDto);
        model.addAttribute("categoryTitle", "review");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/editform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/edit")
    public String updatePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId,
                             @ModelAttribute PostDto postDto,
                             @RequestParam("newCategoryTitle") String newCategoryTitle,
                             @RequestParam("newPostCategoryTitle") String newPostCategoryTitle) {
        Long updatedPostId = editService.updatePost(postId, postDto, "review", newCategoryTitle, newPostCategoryTitle);
        return "redirect:/" + newCategoryTitle + '/' + newPostCategoryTitle + '/' + updatedPostId;
    }
}
