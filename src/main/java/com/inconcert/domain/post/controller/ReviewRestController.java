package com.inconcert.domain.post.controller;

import com.inconcert.domain.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewRestController {
    private final LikeService likeService;

    @PostMapping("/like/{postId}")
    public ResponseEntity<?> toggleLike(@PathVariable("postId") Long postId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", likeService.toggleLike(postId, "review"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/like/status/{postId}")
    public ResponseEntity<Map<String, Boolean>> getLikeStatus(@PathVariable("postId") Long postId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", likeService.isLikedByUser(postId, "review"));
        return ResponseEntity.ok(response);
    }
}