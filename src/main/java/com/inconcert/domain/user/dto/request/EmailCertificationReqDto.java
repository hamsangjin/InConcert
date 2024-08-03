package com.inconcert.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailCertificationReqDto { // 이메일 인증 request DTO
    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;
}
