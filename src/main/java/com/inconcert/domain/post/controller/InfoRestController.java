package com.inconcert.domain.post.controller;

import com.inconcert.domain.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/info")
public class InfoRestController {
    private final LikeService likeService;

    @PostMapping("/like/{postId}")
    public ResponseEntity<Map<String, Boolean>> toggleLike(@PathVariable("postId") Long postId){
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", likeService.toggleLike(postId, "info"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/like/status/{postId}")
    public ResponseEntity<Map<String, Boolean>> getLikeStatus(@PathVariable("postId") Long postId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", likeService.isLikedByUser(postId, "info"));
        return ResponseEntity.ok(response);
    }
}