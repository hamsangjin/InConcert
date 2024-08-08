package com.inconcert.global.auth;

import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.role.repository.RoleRepository;
import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.global.exception.RoleNameNotFoundException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        String email = responseMap.get("email");
        String name = responseMap.get("name");

        // 이메일에서 '@' 앞부분 추출
        String username = extractUsernameFromEmail(email);

        userRepository.findByUsername(username)
                .orElseGet(() -> createUser(username, email, name));

        return new CustomNaverUser(username, oAuth2User.getAttributes(), oAuth2User.getAuthorities());
    }

    private User createUser(String username, String email, String name) {
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNameNotFoundException("Role을 찾을 수 없습니다."))
        );

        // user 테이블 컬럼에서 not null인 요소 기본값으로 지정 후 회원가입 되도록 처리
        User user = User.builder()
                .username(username)
                .password("password")   // 소셜 로그인 시 비밀번호 필요하지 않음 (임의로 비밀번호 지정)
                .email(email)
                .name(name)
                .nickname(username)
                .phoneNumber("010-0000-0000") // 기본으로 저장되는 전화번호
                .birth(LocalDate.now())
                .gender(Gender.FEMALE)  // 기본값
                .mbti(Mbti.INFJ)    // 기본값
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    // 이메일에서 username 정규식 추출
    private String extractUsernameFromEmail(String email) {
        String username = null;
        Pattern pattern = Pattern.compile("^[^@]+");
        Matcher matcher = pattern.matcher(email);
        if (matcher.find()) {
            username = matcher.group();
        }
        return username;
    }
}