package com.inconcert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UsernameCheckReqDto {
    @NotBlank
    private String username;
}
