package com.inconcert.domain.user.dto.request;

import com.inconcert.domain.user.entity.Mbti;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class MyPageEditReqDto {
    @NotBlank
    private String nickname;

    @NotBlank
    private String password;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private Mbti mbti;

    private String intro;

    private MultipartFile profileImage;
}
