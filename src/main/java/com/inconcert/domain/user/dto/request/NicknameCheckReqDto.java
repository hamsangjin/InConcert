package com.inconcert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class NicknameCheckReqDto {
    @NotBlank
    private String nickname;
}
