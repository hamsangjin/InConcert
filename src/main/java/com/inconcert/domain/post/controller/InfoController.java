package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.service.EditService;
import com.inconcert.domain.post.service.InfoService;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/info")
@RequiredArgsConstructor
public class InfoController {
    private final InfoService infoService;
    private final UserService userService;
    private final EditService editService;

    @GetMapping
    public String info(Model model) {
        model.addAttribute("Musicalposts", infoService.getAllInfoPostsByPostCategory("musical"));
        model.addAttribute("Concertposts", infoService.getAllInfoPostsByPostCategory("concert"));
        model.addAttribute("Theaterposts", infoService.getAllInfoPostsByPostCategory("theater"));
        model.addAttribute("Etcposts", infoService.getAllInfoPostsByPostCategory("etc"));
        model.addAttribute("categoryTitle", "info");
        return "board/board";
    }

    @GetMapping("/{postCategoryTitle}")
    public String infoDetail(@PathVariable("postCategoryTitle") String postCategoryTitle, Model model) {
        model.addAttribute("posts", infoService.getAllInfoPostsByPostCategory(postCategoryTitle));
        model.addAttribute("categoryTitle", "info");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/board-detail";
    }

    @GetMapping("/{postCategoryTitle}/{postId}")
    public String getPostDetail(@PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId, Model model) {
        model.addAttribute("post", infoService.getPostById(postId));
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("categoryTitle", "info");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/post-detail";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDto postDto){
        infoService.save(postDto);
        return "redirect:/info";
    }
    //글 삭제
    @PostMapping("/{postCategoryTitle}/{postId}/delete")
    public String deletePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId) {
        infoService.deletePost(postId);
        return "redirect:/info/" + postCategoryTitle;
    }

    //글 수정
    @GetMapping("/{postCategoryTitle}/{postId}/edit")
    public String editPostForm(@PathVariable("postCategoryTitle") String postCategoryTitle,
                           @PathVariable("postId") Long postId, Model model) {
        PostDto postDto = infoService.getPostById(postId);


        model.addAttribute("post", postDto);
        model.addAttribute("categoryTitle", "info");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/editform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/edit")
    public String updatePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId,
                             @ModelAttribute PostDto postDto,
                             @RequestParam("newCategoryTitle") String newCategoryTitle,
                             @RequestParam("newPostCategoryTitle") String newPostCategoryTitle) {
        Long updatedPostId = editService.updatePost(postId, postDto, "info", newCategoryTitle, newPostCategoryTitle);
        return "redirect:/" + newCategoryTitle + '/' + newPostCategoryTitle + '/' + updatedPostId;
    }

}