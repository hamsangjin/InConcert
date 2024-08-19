package com.inconcert.domain.user.dto.request;

import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterReqDto {
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{5,}$")
    private String username;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String certificationNumber;

    @NotBlank
    private String name;

    @NotBlank
    private String nickname;

    @NotBlank
    @Pattern(regexp = "^\\d{10,11}$")
    private String phoneNumber;

    @NotNull
    @Past
    private LocalDate birth;

    @NotNull
    private Gender gender;

    @NotNull
    private Mbti mbti;

    @AssertTrue
    private boolean agreeTerms;
}