package com.inconcert.domain.user.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogInReqDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
