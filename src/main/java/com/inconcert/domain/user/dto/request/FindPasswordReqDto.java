package com.inconcert.domain.user.dto.request;

import lombok.Getter;

@Getter
public class FindPasswordReqDto {
    private String username;
    private String email;
}
