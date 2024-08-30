package com.inconcert.common.controller;

import com.inconcert.common.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
            String imageKey = imageService.extractImageKeyFromUrl( request.get("imageUrl"));
            return imageService.deleteImage(imageKey);
    }
}