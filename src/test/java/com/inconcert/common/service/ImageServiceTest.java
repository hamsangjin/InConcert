package com.inconcert.common.service;

import com.inconcert.common.exception.ExceptionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class ImageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ImageService imageService;

    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);
        imageService = new ImageService(s3Client);
        imageService.bucketName = "test-bucket";
        imageService.cloudFrontUrl = "https://cloudfront.test.com/";
    }

    @Test
    void 이미지_업로드_성공() throws IOException {
        // given
        String savedFileName = UUID.randomUUID().toString() + "_image.png";
        given(multipartFile.getBytes()).willReturn(new byte[]{1, 2, 3});
        given(multipartFile.getOriginalFilename()).willReturn("image.png");
        given(multipartFile.getContentType()).willReturn("image/png");

        // when
        ResponseEntity<?> responseEntity = imageService.uploadImage(multipartFile);

        // then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) responseEntity.getBody();
        assertThat(responseBody).isNotEmpty();
        assertTrue(responseBody.containsKey("url"));
        assertThat(responseBody.get("url")).contains(imageService.cloudFrontUrl);
        assertThat(responseBody.get("url")).contains("_image.png");
    }

    @Test
    void 이미지_업로드_실패() throws IOException {
        // given
        given(multipartFile.getBytes()).willThrow(new IOException());

        // when
        ResponseEntity<?> responseEntity = imageService.uploadImage(multipartFile);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getBody()).isEqualTo(ExceptionMessage.IMAGE_UPLOAD_BAD_REQUEST.getMessage());
    }

    @Test
    void 이미지_삭제_성공() {
        // given
        String imageUrl = "https://cloudfront.test.com/test_image.png";

        // when
        ResponseEntity<String> responseEntity = imageService.deleteImage(imageUrl);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo("이미지가 정상적으로 삭제되었습니다.");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void 이미지_삭제_실패() {
        // given
        String imageUrl = "https://cloudfront.test.com/test_image.png";
        given(s3Client.deleteObject(any(DeleteObjectRequest.class))).willThrow(S3Exception.class);

        // when
        ResponseEntity<String> responseEntity = imageService.deleteImage(imageUrl);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().contains("이미지 삭제 실패"));
    }

    @Test
    void URL_S3_키_추출() {
        // given
        String imageUrl = "https://cloudfront.test.com/test_image.png";

        // when
        String imageKey = imageService.extractImageKeyFromUrl(imageUrl);

        // then
        assertThat(imageKey).isEqualTo("test_image.png");
    }

    @Test
    void 게시글_내용에서_s3_keys_찾기() {
        // given
        String content = "<p><img src=\"https://cloudfront.test.com/test_image1.png\">Here is an image: <img src=\"https://cloudfront.test.com/test_image2.png\"></p>";

        // when
        List<String> imageKeys = imageService.extractImageKeys(content);

        // then
        assertThat(imageKeys.size()).isEqualTo(2);
        assertThat(imageKeys.get(0)).isEqualTo("test_image1.png");
        assertThat(imageKeys.get(1)).isEqualTo("test_image2.png");
    }

    @Test
    void 게시글_내용에서_urls_찾기() {
        // given
        String content = "<p><img src=\"https://cloudfront.test.com/test_image1.png\">Here is an image: <img src=\"https://cloudfront.test.com/test_image2.png\"></p>";

        // when
        Map<String, String> imageMap = imageService.extractImageUrls(content);

        // then
        assertThat(imageMap.size()).isEqualTo(2);
        assertThat(imageMap.get("https://cloudfront.test.com/test_image1.png")).isEqualTo("url1");
        assertThat(imageMap.get("https://cloudfront.test.com/test_image2.png")).isEqualTo("url2");
    }
}