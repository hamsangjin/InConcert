package com.inconcert.domain.comment.controller;

import com.inconcert.domain.comment.dto.CommentCreationDTO;
import com.inconcert.domain.comment.dto.CommentDTO;
import com.inconcert.domain.comment.service.CommentService;
import com.inconcert.domain.notification.service.NotificationService;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.PostCategoryNotFoundException;
import com.inconcert.global.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/{categoryTitle}/{postCategoryTitle}/{postId}/comments")
public class CommentController {
    private final Map<String, CommentService> commentServices;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public CommentController(@Qualifier("infoCommentService") CommentService infoCommentService,
                             @Qualifier("matchCommentService") CommentService matchCommentService,
                             @Qualifier("reviewCommentService") CommentService reviewCommentService,
                             @Qualifier("transferCommentService") CommentService transferCommentService,
                             UserService userService, NotificationService notificationService) {
        this.commentServices = new HashMap<>();
        this.commentServices.put("info", infoCommentService);
        this.commentServices.put("match", matchCommentService);
        this.commentServices.put("review", reviewCommentService);
        this.commentServices.put("transfer", transferCommentService);
        this.commentServices.put("musical", infoCommentService);
        this.commentServices.put("concert", infoCommentService);
        this.commentServices.put("theater", infoCommentService);
        this.commentServices.put("etc", infoCommentService);
        this.userService = userService;
        this.notificationService = notificationService;
    }

    private CommentService getService(String postCategoryTitle) {
        CommentService service = this.commentServices.get(postCategoryTitle.toLowerCase());
        if (service == null) {
            throw new PostCategoryNotFoundException("Invalid post category title: " + postCategoryTitle);
        }
        return service;
    }

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable("postCategoryTitle") String postCategoryTitle,
                                                        @PathVariable("postId") Long postId,
                                                        @RequestParam(defaultValue = "asc") String sort) {
        List<CommentDTO> comments = getService(postCategoryTitle).getCommentDTOsByPostId(postCategoryTitle, postId, sort);
        return ResponseEntity.ok(comments);
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

        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        CommentDTO existingComment = getService(postCategoryTitle).getCommentDTOByBoardTypeAndId(postCategoryTitle, commentId);

        // 댓글 수정 권한 검증
        getService(postCategoryTitle).validateCommentEditAuthorization(existingComment, user);

        getService(postCategoryTitle).updateComment(postCategoryTitle, commentId, commentForm);
        return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{commentId}")
    public String deleteComment(@PathVariable("categoryTitle") String categoryTitle,
                                @PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId,
                                @PathVariable("commentId") Long id) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        CommentDTO dto = getService(postCategoryTitle).getCommentDTOByBoardTypeAndId(postCategoryTitle, id);
        Post post = getService(postCategoryTitle).getPostByCategoryAndId(categoryTitle, postId);

        // 댓글 삭제 권한 검증
        getService(postCategoryTitle).validateCommentDeletion(dto, post, user);

        getService(postCategoryTitle).deleteComment(postCategoryTitle, id);
        return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/new")
    public String createComment(@PathVariable("categoryTitle") String categoryTitle,
                                @PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId,
                                @Valid @ModelAttribute("createForm") CommentCreationDTO commentForm,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
        }

        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        getService(postCategoryTitle).saveComment(postCategoryTitle, postId, user, commentForm);

        Post post = getService(postCategoryTitle).getPostByCategoryAndId(categoryTitle, postId);
        notificationService.createCommentsNotification(post, commentForm.getContent());
        return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reply/{parentId}")
    public String createReply(@PathVariable("categoryTitle") String categoryTitle,
                              @PathVariable("postCategoryTitle") String postCategoryTitle,
                              @PathVariable("postId") Long postId,
                              @PathVariable("parentId") Long parentId,
                              @Valid @ModelAttribute("commentForm") CommentCreationDTO commentForm,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
        }

        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        commentForm.setParent(parentId); // 부모 댓글 ID 설정
        getService(postCategoryTitle).saveReply(postCategoryTitle, postId, parentId, user, commentForm);
        return "redirect:/" + categoryTitle + "/" + postCategoryTitle + "/" + postId;
    }
}
