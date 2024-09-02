package com.inconcert.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FindPasswordReqDto {
    private String username;
    private String email;
}
