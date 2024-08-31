package com.inconcert.common.service;

import com.inconcert.common.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
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

    public ResponseEntity<?> uploadImage(MultipartFile image) {
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

            Map<String, String> imageUrlMap = new HashMap<>();
            imageUrlMap.put("url", fileDownloadUri);
            log.info("저장한 이미지의 url: " + fileDownloadUri);
            return ResponseEntity.ok(imageUrlMap);
        } catch (IOException | S3Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionMessage.IMAGE_UPLOAD_BAD_REQUEST.getMessage());
        }
    }

    public ResponseEntity<String> deleteImage(String imageUrl) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageUrl)
                    .build());
            log.info("삭제한 이미지의 url: " + imageUrl);
            return ResponseEntity.ok("이미지가 정상적으로 삭제되었습니다.");
        } catch (S3Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 삭제 실패: " + e.getMessage());
        }
    }

    // URL에서 S3키를 추출하는 메소드
    public String extractImageKeyFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    // content에서 S3키들을 추출하는 메서드
    public List<String> extractImageKeys(String content) {
        List<String> imageKeys = new ArrayList<>();

        // 이미지 URL을 찾기 위한 정규식 패턴 (이미지 경로만 추출)
        Pattern pattern = Pattern.compile("https://[\\w.-]+/([\\w-]+_[\\w.-]+)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String imageKey = matcher.group(1); // S3 키 추출
            imageKeys.add(imageKey);
        }

        return imageKeys;
    }

    public Map<String, String> extractImageUrls(String content) {
        // 정규표현식으로 img 태그의 src 속성 추출
        Pattern imgPattern = Pattern.compile(
                "<img\\s[^>]*?src=['\"]([^'\"]*?)['\"][^>]*>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        List<String> imageUrls = new ArrayList<>();
        Matcher matcher = imgPattern.matcher(content);

        while (matcher.find()) {
            imageUrls.add(matcher.group(1));  // 첫 번째 그룹은 src 속성의 값
        }

        Map<String, String> imageMap = new HashMap<>();
        for(int i = 0; i < imageUrls.size(); i++){
            imageMap.put(imageUrls.get(i), "url" + (i+1));
        }
        return imageMap;
    }
}