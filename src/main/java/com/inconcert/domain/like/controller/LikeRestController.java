package com.inconcert.domain.like.controller;

import com.inconcert.domain.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LikeRestController {
    private final LikeService likeService;

    @PostMapping("/{categoryTitle}/like/{postId}")
    public ResponseEntity<Map<String, Boolean>> toggleLike(@PathVariable("postId") Long postId,
                                                           @PathVariable("categoryTitle") String categoryTitle){
        return likeService.toggleLike(postId, categoryTitle);
    }

    @GetMapping("{categoryTitle}/like/status/{postId}")
    public ResponseEntity<Map<String, Boolean>> getLikeStatus(@PathVariable("postId") Long postId,
                                                              @PathVariable("categoryTitle") String categoryTitle){
        return likeService.isLikedByUser(postId, categoryTitle);
    }
}