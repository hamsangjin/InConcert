package com.inconcert.domain.comment.controller;

import com.inconcert.domain.comment.dto.CommentDto;
import com.inconcert.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
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
}
