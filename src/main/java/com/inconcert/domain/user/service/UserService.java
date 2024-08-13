package com.inconcert.domain.user.service;

import com.inconcert.domain.certification.common.CertificationNumber;
import com.inconcert.domain.certification.common.TempPassword;
import com.inconcert.domain.certification.entity.Certification;
import com.inconcert.domain.certification.provider.EmailProvider;
import com.inconcert.domain.certification.provider.TempPasswordEmailProvider;
import com.inconcert.domain.certification.repository.CertificationRepository;
import com.inconcert.domain.role.entity.Role;
import com.inconcert.domain.role.repository.RoleRepository;
import com.inconcert.domain.user.dto.request.*;
import com.inconcert.domain.user.dto.response.*;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.global.dto.ResponseDto;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.RoleNameNotFoundException;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;
    private final RoleRepository roleRepository;
    private final EmailProvider emailProvider;
    private final TempPasswordEmailProvider tempPasswordEmailProvider;

    private final PasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 아이디 중복 확인
    public ResponseEntity<? super UsernameCheckRspDto> usernameCheck(UsernameCheckReqDto reqDto) {
        try {
            String username = reqDto.getUsername();
            boolean isExistUserId = userRepository.existsByUsername(username);
            if(isExistUserId) return UsernameCheckRspDto.duplicateId();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseDto.databaseError();
        }

        return UsernameCheckRspDto.success();
    }

    // 이메일 중복 확인
    public ResponseEntity<? super EmailCheckRspDto> emailCheck(EmailCheckReqDto reqDto) {
        try {
            String email = reqDto.getEmail();
            boolean isExistEmail = userRepository.existsByEmail(email);
            if(isExistEmail) return EmailCheckRspDto.duplicateEmail();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseDto.databaseError();
        }

        return EmailCheckRspDto.success();
    }

    // 닉네임 중복 확인
    public ResponseEntity<? super NicknameCheckRspDto> nicknameCheck(NicknameCheckReqDto reqDto) {
        try {
            String nickname = reqDto.getNickname();
            boolean isExistNickname = userRepository.existsByNickname(nickname);
            if(isExistNickname) return NicknameCheckRspDto.duplicateNickname();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseDto.databaseError();
        }

        return NicknameCheckRspDto.success();
    }

    // 인증 메일 전송
    @Transactional
    public ResponseEntity<? super EmailCertificationRspDto> emailCertification(EmailCertificationReqDto reqDto) {
        try{
            String username = reqDto.getUsername();
            String email = reqDto.getEmail();

            boolean isExistUsername = userRepository.existsByUsername(username);
            if(isExistUsername) return UsernameCheckRspDto.duplicateId();    // id가 중복될 경우 (username)

            String certificationNumber = CertificationNumber.certificationNumber();

            // 메일 전송
            boolean isSucceed = emailProvider.sendEmail(email, certificationNumber);
            if(!isSucceed) return EmailCertificationRspDto.mailSendFail();

            Certification certification = new Certification(email, certificationNumber, username);
            certificationRepository.save(certification);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseDto.databaseError();
        }

        return EmailCertificationRspDto.success();
    }

    // 인증 번호 확인
    public ResponseEntity<? super CheckCertificationRspDto> checkCertification(CheckCertificationReqDto reqDto) {
        try {
            String username = reqDto.getUsername();
            String email = reqDto.getEmail();
            String certificationNumber = reqDto.getCertificationNumber();

            Certification certification = certificationRepository.findByUsername(username);
            if(certification == null) return CheckCertificationRspDto.certificationFail();

            boolean isMatched = certification.getEmail().equals(email) && certification.getCertificationNumber().equals(certificationNumber);
            if(!isMatched) return CheckCertificationRspDto.certificationFail(); // 인증 번호가 일치하지 않을 때
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseDto.databaseError();
        }

        return CheckCertificationRspDto.success();
    }

    // 회원가입
    @Transactional
    public ResponseEntity<? super RegisterRspDto> joinUser(RegisterReqDto reqDto) {
        try {
            String username = reqDto.getUsername();
            boolean isExistUsername = userRepository.existsByUsername(username);
            if(isExistUsername) return RegisterRspDto.duplicateId();

            String email = reqDto.getEmail();
            String certificationNumber = reqDto.getCertificationNumber();

            Certification certification = certificationRepository.findByUsername(username);

            boolean isMatched = certification.getEmail().equals(email) && certification.getCertificationNumber().equals(certificationNumber);
            if(!isMatched) return RegisterRspDto.certificationFail();

            String password = reqDto.getPassword();
            String encodedPassword = passwordEncoder.encode(password);

            // ROLE_USER를 데이터베이스에서 조회
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RoleNameNotFoundException("Role을 찾을 수 없습니다."));

            Set<Role> roles = new HashSet<>();
            roles.add(userRole);

            User user = User.builder()
                    .username(username)
                    .password(encodedPassword)
                    .email(email)
                    .name(reqDto.getName())
                    .nickname(reqDto.getNickname())
                    .phoneNumber(reqDto.getPhoneNumber())
                    .birth(reqDto.getBirth())
                    .gender(reqDto.getGender())
                    .mbti(reqDto.getMbti())
                    .roles(roles)
                    .build();

            userRepository.save(user);
            certificationRepository.delete(certification); // 회원가입이 되면 안증번호 내역 지우기

            return RegisterRspDto.success();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseDto.databaseError();
        }
    }

    // 아이디 찾기
    public String findUserId(FindIdReqDto reqDto) {
        User findUser = userRepository.findByNameAndEmail(reqDto.getName(), reqDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        return findUser.getUsername();
    }

    // 비밀번호 찾기
    @Transactional
    public User findPassword(FindPasswordReqDto reqDto) {
        User user = userRepository.findByUsernameAndEmail(reqDto.getUsername(), reqDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        // 임시 비밀번호
        String tempPassword = TempPassword.certificationNumber();

        // 메일 전송
        tempPasswordEmailProvider.sendEmail(reqDto.getEmail(), tempPassword);

        // 해당 비밀번호로 유저 정보 수정
        String encodePassword = passwordEncoder.encode(tempPassword);
        user.updatePassword(encodePassword);

        return userRepository.save(user);
    }

    public Optional<User> getAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            String username = null;

            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            }

            return userRepository.findByUsername(username);
        } catch (Exception e) {
            log.error("Error getting authenticated user", e);
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteUser(){
        User user = getAuthenticatedUser().orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        userRepository.deleteById(user.getId());
    }
}
