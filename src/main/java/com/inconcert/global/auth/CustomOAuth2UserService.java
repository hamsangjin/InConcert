package com.inconcert.global.auth;

import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.role.repository.RoleRepository;
import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.RoleNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        try {
            return processOAuth2User(provider, oAuth2User);
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("회원가입 또는 로그인에 실패하였습니다.");
        }
    }

    private OAuth2User processOAuth2User(String provider, OAuth2User oAuth2User) {
        if (!"naver".equals(provider)) {
            throw new OAuth2AuthenticationException("제공되지 않는 소셜 서비스 입니다.: " + provider);
        }

        Map<String, String> responseMap = (Map<String, String>) oAuth2User.getAttributes().get("response");

        String username = responseMap.get("id");
        String email = responseMap.get("email");
        String name = responseMap.get("name");
        String gender = responseMap.get("gender");
        String mobile = responseMap.get("mobile").replace("-", "");
        LocalDate birth = LocalDate.parse(responseMap.get("birthyear").concat("-" + responseMap.get("birthday")));

        userRepository.findByUsername(username)
                .orElseGet(() -> createUser(username, email, name, gender, mobile, birth));

        return new CustomNaverUser(username, oAuth2User.getAttributes(), oAuth2User.getAuthorities());
    }

    private User createUser(String username, String email, String name, String gender, String mobile, LocalDate birth) {
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException(ExceptionMessage.ROLE_NOT_FOUND.getMessage()))
        );

        User user = User.builder()
                .username(username)
                .password("password")   // 소셜 로그인 시 비밀번호 필요하지 않음 (임의로 비밀번호 지정)
                .email(email)
                .name(name)
                .nickname("inconcert" + username.substring(0, 8))   // 임시 닉네임
                .phoneNumber(mobile)
                .birth(birth)
                .gender(gender.equals("F") ? Gender.FEMALE : Gender.MALE)
                .mbti(Mbti.미선택)
                .roles(roles)
                .build();

        return userRepository.save(user);
    }
}