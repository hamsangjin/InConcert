package com.inconcert.common.controller;

import com.inconcert.common.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ImageApiController {
    private final ImageService imageService;

    @PostMapping("/uploadImage")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image){
        return imageService.uploadImage(image);
    }

    @PostMapping("/deleteImage")
    public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> request) {
            String imageKey = imageService.extractImageKeyFromUrl(request.get("imageUrl"));
            return imageService.deleteImage(imageKey);
    }

    @PostMapping("/api/extract-images")
    public ResponseEntity<?> uploadImage(@RequestBody Map<String, String> content) {
        return ResponseEntity.ok(imageService.extractImageUrls(content.get("content")));
    }
}