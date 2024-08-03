package com.inconcert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailCheckReqDto { // 이메일 중복 확인
    @NotBlank
    private String email;
}
