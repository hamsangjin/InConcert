package com.inconcert.domain.comment.controller;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.comment.dto.CommentDto;
import com.inconcert.domain.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/boards/{boardType}/comments")
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
    }

    private CommentService getService(String boardType) {
        return this.commentServices.get(boardType.toLowerCase());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable("boardType") String boardType, @PathVariable("postId") Long postId) {
        List<CommentDto> byPostId = getService(boardType).findByPostId(boardType, postId);
        return ResponseEntity.ok(byPostId);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable("boardType") String boardType, @PathVariable("commentId") Long commentId) {
        CommentDto commentDto = getService(boardType).findComment(boardType, commentId);
        return ResponseEntity.ok(commentDto);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/edit/{postId}/{commentId}")
    public String editComment(@PathVariable("boardType") String boardType, @PathVariable("postId") Long postId, @PathVariable("commentId") Long id, CommentCreateForm createForm) {
        Long commentId = getService(boardType).updateComment(boardType, id, createForm);
        return "redirect:/boards/" + boardType + "/" + postId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{postId}/{commentId}")
    public String deleteComment(@PathVariable("boardType") String boardType, @PathVariable("postId") Long postId, @PathVariable("commentId") Long id) {
        CommentDto dto = getService(boardType).findComment(boardType, id);
        getService(boardType).deleteComment(boardType, id);
        return "redirect:/boards/" + boardType + "/" + postId;
    }
}
