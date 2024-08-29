package com.inconcert.domain.post.service;
import com.inconcert.common.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.cloudfront.url}")
    private String cloudFrontUrl;

    private final S3Client s3Client;

    public Map<String, String> uploadImage(MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String originalFileName = file.getOriginalFilename();
        String savedFileName = uuid + "_" + (originalFileName != null ? originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_") : "");

        try {
            // S3에 파일 업로드
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(savedFileName)
                            .contentType(file.getContentType()) // Content-Type 설정
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

            // CloudFront URL 생성
            String fileDownloadUri = cloudFrontUrl + savedFileName;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileDownloadUri);
            return result;
        } catch (IOException e) {
            throw new ImageUploadException("Failed to read the file: " + e.getMessage());
        } catch (S3Exception e) {
            // S3 관련 예외 처리
            throw new ImageUploadException("Failed to upload image to S3: " + e.awsErrorDetails().errorMessage());
        }
    }
}