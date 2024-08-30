package com.inconcert.common.service;

import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.cloudfront.url}")
    private String cloudFrontUrl;

    private final S3Client s3Client;

    // 저장되는 이미지 파일명
    public String generateTempImageName(MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String originalFileName = file.getOriginalFilename();
        return uuid + "_" + (originalFileName != null ? originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_") : "");
    }

    public ResponseEntity<?> uploadImages(List<MultipartFile> images) {
        List<Map<String, String>> results = new ArrayList<>();
        for (MultipartFile image : images) {
            String savedFileName = generateTempImageName(image);

            try {
                // S3에 파일 업로드
                s3Client.putObject(PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(savedFileName)
                                .contentType(image.getContentType()) // Content-Type 설정
                                .build(),
                        software.amazon.awssdk.core.sync.RequestBody.fromBytes(image.getBytes()));

                // CloudFront URL 생성
                String fileDownloadUri = cloudFrontUrl + savedFileName;

                Map<String, String> result = new HashMap<>();
                result.put("url", fileDownloadUri);
                results.add(result);
            } catch (IOException | S3Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionMessage.IMAGE_UPLOAD_BAD_REQUEST.getMessage());
            }
        }

        return ResponseEntity.ok(results);
    }

    // 수정 필요
    public void deleteImage(String imageKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageKey)
                    .build());
        } catch (S3Exception e) {
            throw new ImageUploadException("Failed to delete the file: " + e.getMessage());
        }
    }
}