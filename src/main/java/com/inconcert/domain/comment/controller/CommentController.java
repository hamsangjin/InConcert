package com.inconcert.domain.comment.controller;

import com.inconcert.domain.comment.dto.CommentCreationDTO;
import com.inconcert.domain.comment.service.CommentService;
import com.inconcert.common.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/{categoryTitle}/{postCategoryTitle}/{postId}/comments")
public class CommentController {
    private final Map<String, CommentService> commentServices;

    @Autowired
    public CommentController(@Qualifier("infoCommentService") CommentService infoCommentService,
                             @Qualifier("matchCommentService") CommentService matchCommentService,
                             @Qualifier("reviewCommentService") CommentService reviewCommentService,
                             @Qualifier("transferCommentService") CommentService transferCommentService) {
        this.commentServices = new HashMap<>();
        this.commentServices.put("info", infoCommentService);
        this.commentServices.put("match", matchCommentService);
        this.commentServices.put("review", reviewCommentService);
        this.commentServices.put("transfer", transferCommentService);
        this.commentServices.put("musical", infoCommentService);
        this.commentServices.put("concert", infoCommentService);
        this.commentServices.put("theater", infoCommentService);
        this.commentServices.put("etc", infoCommentService);
    }

    private CommentService getService(String postCategoryTitle) {
        return this.commentServices.get(postCategoryTitle.toLowerCase());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/edit/{commentId}")
    public String editComment(@PathVariable("categoryTitle") String categoryTitle,
                              @PathVariable("postCategoryTitle") String postCategoryTitle,
                              @PathVariable("postId") Long postId,
                              @PathVariable("commentId") Long commentId,
                              @Valid @ModelAttribute("commentForm") CommentCreationDTO commentForm,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
        }

        getService(postCategoryTitle).updateComment(postCategoryTitle, commentId, commentForm);
        return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{commentId}")
    public String deleteComment(@PathVariable("categoryTitle") String categoryTitle,
                                @PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId,
                                @PathVariable("commentId") Long id) {
        getService(postCategoryTitle).deleteComment(postCategoryTitle, id);
        return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/new")
    public String createComment(@PathVariable("categoryTitle") String categoryTitle,
                                @PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId,
                                @Valid @ModelAttribute("createForm") CommentCreationDTO commentForm,
                                BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
        }

        try {
            getService(postCategoryTitle).saveComment(postCategoryTitle, postId, commentForm);
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "로그인 후 이용 가능합니다.");
            return "redirect:/loginform";
        }

        return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reply/{parentId}")
    public String createReply(@PathVariable("categoryTitle") String categoryTitle,
                              @PathVariable("postCategoryTitle") String postCategoryTitle,
                              @PathVariable("postId") Long postId,
                              @PathVariable("parentId") Long parentId,
                              @Valid @ModelAttribute("commentForm") CommentCreationDTO commentForm,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
        }

        try {
            getService(postCategoryTitle).saveReply(postCategoryTitle, postId, parentId, commentForm);
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "로그인 후 이용 가능합니다.");
            return "redirect:/loginform";
        }

        return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
    }
}