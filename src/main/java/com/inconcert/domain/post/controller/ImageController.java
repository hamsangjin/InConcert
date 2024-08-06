package com.inconcert.domain.post.controller;

import com.inconcert.domain.post.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/uploadImage")
    @ResponseBody
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file){
        return imageService.uploadImage(file);
    }
}