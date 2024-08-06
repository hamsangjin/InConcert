package com.inconcert.domain.post.controller;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.EditService;
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
        model.addAttribute("createForm", new CommentCreateForm());
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

    //글 삭제
    @PostMapping("/{postCategoryTitle}/{postId}/delete")
    public String deletePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId) {
        matchService.deletePost(postId);
        return "redirect:/match/" + postCategoryTitle;
    }

    //글 수정
    @GetMapping("/{postCategoryTitle}/{postId}/edit")
    public String editPostForm(@PathVariable("postCategoryTitle") String postCategoryTitle,
                               @PathVariable("postId") Long postId, Model model) {
        PostDto postDto = matchService.getPostById(postId);


        model.addAttribute("post", postDto);
        model.addAttribute("categoryTitle", "match");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/editform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/edit")
    public String updatePost(@PathVariable("postId") Long postId,
                             @ModelAttribute PostDto postDto,
                             @RequestParam("newCategoryTitle") String newCategoryTitle,
                             @RequestParam("newPostCategoryTitle") String newPostCategoryTitle) {
        Long updatedPostId = editService.updatePost(postId, postDto, "match", newCategoryTitle, newPostCategoryTitle);
        return "redirect:/" + newCategoryTitle + '/' + newPostCategoryTitle + '/' + updatedPostId;
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDto postDto) {
        matchService.save(postDto);
        return "redirect:/match";
    }
}
