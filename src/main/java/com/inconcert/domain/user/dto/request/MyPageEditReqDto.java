package com.inconcert.domain.user.dto.request;

import com.inconcert.domain.user.entity.Mbti;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class MyPageEditReqDto {
    @NotBlank
    @Size(max = 8, message = "닉네임은 8자 이내로 입력해주세요.")
    private String nickname;

    @NotBlank
    private String password;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private Mbti mbti;

    private String intro;

    private MultipartFile profileImage;
}
