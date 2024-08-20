package com.inconcert.domain.chat.dto;

import com.inconcert.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;

    // 엔티티로부터 DTO 생성하는 팩토리 메소드
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getUsername());
    }
}
