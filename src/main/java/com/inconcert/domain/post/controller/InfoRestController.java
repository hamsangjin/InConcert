package com.inconcert.domain.post.controller;

import com.inconcert.domain.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/info")
public class InfoRestController {
    private final LikeService likeService;

    @PostMapping("/like/{postId}")
    public ResponseEntity<?> toggleLike(@PathVariable("postId") Long postId){
        boolean result = likeService.toggleLike(postId, "info");
        if(result){
            return ResponseEntity.ok("성공");
        }else return ResponseEntity.badRequest().body("실패다 임마");
    }
}
