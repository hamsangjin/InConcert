package com.inconcert.domain.images.controller;

import com.inconcert.domain.images.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ImageApiController {
    private final ImageService imageService;

    @PostMapping("/uploadImages")
    public ResponseEntity<?> uploadImages(@RequestParam("images") List<MultipartFile> images){
        return imageService.uploadImages(images);
    }
}