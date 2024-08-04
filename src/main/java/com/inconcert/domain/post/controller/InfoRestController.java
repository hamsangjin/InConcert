package com.inconcert.domain.post.controller;

import com.inconcert.domain.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/info")
public class InfoRestController {
    private final LikeService likeService;


    @PostMapping("/like/{categoryTitle}/{postId}")
    public ResponseEntity<?> toggleLike(@PathVariable("categoryTitle")String categoryTitle,@PathVariable("postId") Long postId){
        boolean result = likeService.toggleLike(postId,categoryTitle);
        if(result){
            return ResponseEntity.ok("성공");
        }else return ResponseEntity.badRequest().body("실패다 임마");
    }



}
