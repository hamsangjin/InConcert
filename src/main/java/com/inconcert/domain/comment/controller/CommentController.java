package com.inconcert.domain.comment.controller;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.comment.dto.CommentDto;
import com.inconcert.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable("postId") Long postId) {
        List<CommentDto> byPostId = commentService.findByPostId(postId);
        return ResponseEntity.ok(byPostId);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable("commentId") Long commentId) {
        CommentDto commentDto = commentService.findComment(commentId);
        return ResponseEntity.ok(commentDto);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/edit/{postId}/{commentId}")
    public String editComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long id, CommentCreateForm createForm) {
        Long commentId = commentService.updateComment(id, createForm);
        return "redirect:/board/" + postId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{postId}/{commentId}")
    public String deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long id) {
        CommentDto dto = commentService.findComment(id);
        commentService.deleteComment(id);
        return "redirect:/board/" + postId;
    }
}
