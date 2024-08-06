package com.inconcert.domain.post.service;

import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.ImageUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService {
    @Value("${image.upload.dir}")
    private String uploadDir;

    public Map<String, String> uploadImage(MultipartFile file){
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "_" + file.getOriginalFilename();
        Path savePath = Paths.get(uploadDir, savedFileName);

        try {
            // 디렉토리가 존재하지 않으면 생성
            if (!Files.exists(savePath.getParent())) {
                Files.createDirectories(savePath.getParent());
            }

            Files.write(savePath, file.getBytes());

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/images/")
                    .path(savedFileName)
                    .toUriString();

            Map<String, String> result = new HashMap<>();
            result.put("url", fileDownloadUri);
            return result;
        } catch (IOException e) {
            throw new ImageUploadException(ExceptionMessage.IMAGE_UPLOAD_BAD_REQUEST.getMessage());
        }
    }
}
