package com.inconcert.domain.user.dto.response;

import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRspDTO {
    private Long reviewerId;
    private Long revieweeId;
    private Long postId;
    private String profileImage;
    private String nickname;
    private LocalDate birth;
    private Mbti mbti;
    private Gender gender;

    public FeedbackRspDTO(Long revieweeId, String profileImage, String nickname, LocalDate birth, Mbti mbti, Gender gender, Long reviewerId, Long postId) {
        this.revieweeId = revieweeId;
        this.profileImage = profileImage;
        this.nickname = nickname;
        this.birth = birth;
        this.mbti = mbti;
        this.gender = gender;
        this.reviewerId = reviewerId;
        this.postId = postId;
    }
}
