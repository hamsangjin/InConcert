package com.inconcert.domain.post.controller;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.EditService;
import com.inconcert.domain.post.service.TransferService;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;
    private final UserService userService;
    private final EditService editService;

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
        model.addAttribute("createForm", new CommentCreateForm());

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
        Map<String, String> searchInfo = new HashMap<>();
        searchInfo.put("period", period);
        searchInfo.put("type", type);
        searchInfo.put("keyword", keyword);
        model.addAttribute("searchInfo", searchInfo);
        return "board/board-detail";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/delete")
    public String deletePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId) {
        transferService.deletePost(postId);
        return "redirect:/transfer/" + postCategoryTitle;
    }

    @GetMapping("/{postCategoryTitle}/{postId}/edit")
    public String editPostForm(@PathVariable("postCategoryTitle") String postCategoryTitle,
                               @PathVariable("postId") Long postId, Model model) {
        PostDto postDto = transferService.getPostById(postId);


        model.addAttribute("post", postDto);
        model.addAttribute("categoryTitle", "transfer");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/editform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/edit")
    public String updatePost(@PathVariable("postId") Long postId,
                             @ModelAttribute PostDto postDto,
                             @RequestParam("newCategoryTitle") String newCategoryTitle,
                             @RequestParam("newPostCategoryTitle") String newPostCategoryTitle) {
        Long updatedPostId = editService.updatePost(postId, postDto, "transfer", newCategoryTitle, newPostCategoryTitle);
        return "redirect:/" + newCategoryTitle + '/' + newPostCategoryTitle + '/' + updatedPostId;
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDto postDto) {
        transferService.save(postDto);
        return "redirect:/transfer";
    }
}
