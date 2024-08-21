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
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.RoleNotFoundException;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;
    private final RoleRepository roleRepository;
    private final EmailProvider emailProvider;
    private final TempPasswordEmailProvider tempPasswordEmailProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
    }

    // 아이디 중복 확인
    @Transactional(readOnly = true)
    public ResponseEntity<? super UsernameCheckRspDto> checkUsername(UsernameCheckReqDto reqDto) {
        String username = reqDto.getUsername();
        boolean isExistUserId = userRepository.existsByUsername(username);
        if(isExistUserId) return UsernameCheckRspDto.duplicateId();

        return UsernameCheckRspDto.success();
    }

    // 이메일 중복 확인
    @Transactional(readOnly = true)
    public ResponseEntity<? super EmailCheckRspDto> checkEmail(EmailCheckReqDto reqDto) {
        String email = reqDto.getEmail();
        boolean isExistEmail = userRepository.existsByEmail(email);
        if(isExistEmail) return EmailCheckRspDto.duplicateEmail();

        return EmailCheckRspDto.success();
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public ResponseEntity<? super NicknameCheckRspDto> checkNickname(NicknameCheckReqDto reqDto) {
        String nickname = reqDto.getNickname();
        boolean isExistNickname = userRepository.existsByNickname(nickname);
        if(isExistNickname) return NicknameCheckRspDto.duplicateNickname();

        return NicknameCheckRspDto.success();
    }

    // 전화번호 중복 확인
    @Transactional(readOnly = true)
    public ResponseEntity<? super PhoneNumberCheckRspDto> checkPhoneNumber(PhoneNumberCheckReqDto reqDto) {
        String phoneNumber = reqDto.getPhoneNumber();
        boolean isExistPhoneNumber = userRepository.existsByPhoneNumber(phoneNumber);
        if(isExistPhoneNumber) return PhoneNumberCheckRspDto.duplicatePhoneNumber();

        return PhoneNumberCheckRspDto.success();
    }

    // 인증 메일 전송 (인증번호 갱신 또는 생성)
    @Transactional
    public ResponseEntity<? super EmailCertificationRspDto> sendCertificationNumber(EmailCertificationReqDto reqDto) {
        String username = reqDto.getUsername();
        String email = reqDto.getEmail();

        boolean isExistUsername = userRepository.existsByUsername(username);
        if(isExistUsername) return UsernameCheckRspDto.duplicateId();    // id가 중복될 경우 (username)

        // 새로운 인증번호 생성
        String certificationNumber = CertificationNumber.certificationNumber();

        // 메일 전송
        boolean isSucceed = emailProvider.sendEmail(email, certificationNumber);
        if(!isSucceed) return EmailCertificationRspDto.mailSendFail();

        // 해당 사용자에 대한 인증번호가 이미 존재하는지 확인
        Certification existingCertification = certificationRepository.findByUsername(username);

        Certification certification;
        if (existingCertification != null) {
            certification = Certification.builder()
                    .id(existingCertification.getId())
                    .email(email)
                    .certificationNumber(certificationNumber)
                    .username(username)
                    .build();
        }
        else {
            // 새로운 인증번호 객체 생성
            certification = Certification.builder()
                    .email(email)
                    .certificationNumber(certificationNumber)
                    .username(username)
                    .build();
        }

        certificationRepository.save(certification);

        return EmailCertificationRspDto.success();
    }

    // 인증 번호 확인
    @Transactional(readOnly = true)
    public ResponseEntity<? super CheckCertificationRspDto> checkCertification(CheckCertificationReqDto reqDto) {
        String username = reqDto.getUsername();
        String email = reqDto.getEmail();
        String certificationNumber = reqDto.getCertificationNumber();

        Certification certification = certificationRepository.findByUsername(username);
        if(certification == null) return CheckCertificationRspDto.certificationFail();

        boolean isMatched = certification.getEmail().equals(email) && certification.getCertificationNumber().equals(certificationNumber);
        if(!isMatched) return CheckCertificationRspDto.certificationFail(); // 인증 번호가 일치하지 않을 때

        return CheckCertificationRspDto.success();
    }

    // 회원가입
    @Transactional
    public ResponseEntity<? super RegisterRspDto> joinUser(RegisterReqDto reqDto) {
        String username = reqDto.getUsername();
        boolean isExistUsername = userRepository.existsByUsername(username);
        if(isExistUsername) return RegisterRspDto.duplicateId();

        // 비밀번호 확인
        if (!reqDto.getPassword().equals(reqDto.getPasswordConfirm())) {
            return RegisterRspDto.passwordNotMatch();
        }

        String email = reqDto.getEmail();
        String certificationNumber = reqDto.getCertificationNumber();

        Certification certification = certificationRepository.findByUsername(username);

        boolean isMatched = certification.getEmail().equals(email) && certification.getCertificationNumber().equals(certificationNumber);
        if(!isMatched) return RegisterRspDto.certificationFail();

        String password = reqDto.getPassword();
        String encodedPassword = passwordEncoder.encode(password);

        // ROLE_USER를 데이터베이스에서 조회
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException(ExceptionMessage.ROLE_NOT_FOUND.getMessage()));

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

    // 아이디 찾기
    @Transactional(readOnly = true)
    public String findUserId(FindIdReqDto reqDto) {
        User findUser = userRepository.findByNameAndEmail(reqDto.getName(), reqDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        return findUser.getUsername();
    }

    // 비밀번호 찾기
    @Transactional
    public User findPassword(FindPasswordReqDto reqDto) {
        User user = userRepository.findByUsernameAndEmail(reqDto.getUsername(), reqDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 임시 비밀번호
        String tempPassword = TempPassword.certificationNumber();

        // 메일 전송
        tempPasswordEmailProvider.sendEmail(reqDto.getEmail(), tempPassword);

        // 해당 비밀번호로 유저 정보 수정
        String encodePassword = passwordEncoder.encode(tempPassword);
        user.updatePassword(encodePassword);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
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
            log.error("getAuthenticatedUser 메소드에서 오류난거임 오류 클래스 생성하세요 !!!!", e);
            return Optional.empty();
        }
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(){
        User user = getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        userRepository.deleteById(user.getId());
    }
}