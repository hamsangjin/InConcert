package com.inconcert.domain.post.controller;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.service.EditService;
import com.inconcert.domain.post.service.ReviewService;
import com.inconcert.domain.post.service.WriteService;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;
    private final EditService editService;
    private final WriteService writeService;

    @GetMapping
    public String review(Model model,
                         @RequestParam(name = "page", defaultValue = "0") int page,
                         @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<PostDto> postsPage = reviewService.getAllInfoPostsByPostCategory(page, size);

        model.addAttribute("postsPage", postsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("categoryTitle", "review");
        return "board/board-detail";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "keyword") String keyword,
                         @RequestParam(name = "period", required = false, defaultValue = "all") String period,
                         @RequestParam(name = "type", required = false, defaultValue = "title+content") String type,
                         @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                         @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                         Model model) {

        Page<PostDto> postsPage = reviewService.findByKeywordAndFilters(keyword, period, type, page, size);

        Map<String, String> searchInfo = new HashMap<>();
        searchInfo.put("period", period);
        searchInfo.put("type", type);
        searchInfo.put("keyword", keyword);
        model.addAttribute("searchInfo", searchInfo);

        model.addAttribute("categoryTitle", "review");
        model.addAttribute("postsPage", postsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());

        return "board/board-detail";
    }

    @GetMapping("/{postCategoryTitle}/{postId}")
    public String getPostDetail(@PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId, Model model) {
        model.addAttribute("post", reviewService.getPostDtoByPostId(postId));
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("categoryTitle", "review");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        model.addAttribute("createForm", new CommentCreateForm());

        return "board/post-detail";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/delete")
    public String deletePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId) {
        reviewService.deletePost(postId);
        return "redirect:/review/" + postCategoryTitle;
    }

    @GetMapping("/{postCategoryTitle}/{postId}/edit")
    public String editPostForm(@PathVariable("postCategoryTitle") String postCategoryTitle,
                               @PathVariable("postId") Long postId, Model model) {
        PostDto postDto = reviewService.getPostDtoByPostId(postId);


        model.addAttribute("post", postDto);
        model.addAttribute("categoryTitle", "review");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/editform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/edit")
    public String updatePost(@PathVariable("postId") Long postId,
                             @ModelAttribute PostDto postDto,
                             @RequestParam("newCategoryTitle") String newCategoryTitle,
                             @RequestParam("newPostCategoryTitle") String newPostCategoryTitle) {
        Long updatedPostId = editService.updatePost(postId, postDto, "review", newCategoryTitle, newPostCategoryTitle);
        return "redirect:/" + newCategoryTitle + '/' + newPostCategoryTitle + '/' + updatedPostId;
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDto postDto) {
        Post post = writeService.save(postDto);
        return "redirect:/review/" + post.getPostCategory().getTitle() + '/' + post.getId();
    }
}