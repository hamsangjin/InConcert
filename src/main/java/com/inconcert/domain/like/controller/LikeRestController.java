package com.inconcert.domain.like.controller;

import com.inconcert.domain.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LikeRestController {
    private final LikeService likeService;

    @PostMapping("/{categoryTitle}/like/{postId}")
    public ResponseEntity<Map<String, Boolean>> toggleLike(@PathVariable("postId") Long postId,
                                                           @PathVariable("categoryTitle") String categoryTitle){
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", likeService.toggleLike(postId, categoryTitle));
        return ResponseEntity.ok(response);
    }

    @GetMapping("{categoryTitle}/like/status/{postId}")
    public ResponseEntity<Map<String, Boolean>> getLikeStatus(@PathVariable("postId") Long postId,
                                                              @PathVariable("categoryTitle") String categoryTitle){
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", likeService.isLikedByUser(postId, categoryTitle));
        return ResponseEntity.ok(response);
    }
}