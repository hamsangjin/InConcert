package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/uploadImages")
    public ResponseEntity<?> uploadImages(@RequestParam("images") List<MultipartFile> images){
        return imageService.uploadImages(images);
    }
}