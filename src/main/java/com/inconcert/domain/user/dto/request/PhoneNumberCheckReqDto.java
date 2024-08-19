package com.inconcert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PhoneNumberCheckReqDto {
    @NotBlank
    private String phoneNumber;
}
