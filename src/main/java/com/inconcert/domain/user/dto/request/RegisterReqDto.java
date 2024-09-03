package com.inconcert.domain.user.dto.request;

import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReqDto {
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{5,}$")
    private String username;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    private String password;

    @NotBlank
    private String passwordConfirm;

    @NotBlank
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    @NotBlank
    private String certificationNumber;

    @NotBlank
    @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글 또는 영어만 입력 가능합니다.")
    private String name;

    @NotBlank
    private String nickname;

    @NotBlank
    @Pattern(regexp = "^010\\d{7,8}$", message = "010으로 시작하는 번호를 숫자만 입력해주세요.")
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