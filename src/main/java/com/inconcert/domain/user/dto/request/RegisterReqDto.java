package com.inconcert.domain.user.dto.request;

import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterReqDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String email;

    @NotBlank
    private String certificationNumber;

    @NotBlank
    private String name;

    @NotBlank
    private String nickname;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private LocalDate birth;

    @NotBlank
    private Gender gender;

    @NotBlank
    private Mbti mbti;
}
