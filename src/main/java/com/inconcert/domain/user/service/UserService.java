package com.inconcert.domain.user.service;

import com.inconcert.domain.certification.util.CertificationNumber;
import com.inconcert.domain.certification.util.TempPassword;
import com.inconcert.domain.certification.entity.Certification;
import com.inconcert.domain.certification.provider.EmailProvider;
import com.inconcert.domain.certification.provider.TempPasswordEmailProvider;
import com.inconcert.domain.certification.repository.CertificationRepository;
import com.inconcert.domain.user.dto.request.*;
import com.inconcert.domain.user.dto.response.*;
import com.inconcert.domain.user.entity.Role;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.common.auth.CustomUserDetails;
import com.inconcert.common.auth.jwt.token.entity.Token;
import com.inconcert.common.auth.jwt.token.service.TokenService;
import com.inconcert.common.auth.jwt.util.JwtTokenizer;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.UserNotFoundException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;
    private final EmailProvider emailProvider;
    private final TempPasswordEmailProvider tempPasswordEmailProvider;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;

    // 회원가입
    @Transactional
    public ResponseEntity<? super RegisterRspDto> joinUser(RegisterReqDto reqDto) {
        String username = reqDto.getUsername();
        boolean isExistUsername = userRepository.existsByUsername(username);
        if(isExistUsername) return RegisterRspDto.duplicateId();

        // 비밀번호 확인
        if (!reqDto.getPassword().equals(reqDto.getPasswordConfirm())) {
            return RegisterRspDto.notMatchPassword();
        }

        String email = reqDto.getEmail();
        String certificationNumber = reqDto.getCertificationNumber();

        Certification certification = certificationRepository.findByUsername(username);

        boolean isMatched = certification.getEmail().equals(email) && certification.getCertificationNumber().equals(certificationNumber);
        if(!isMatched) return RegisterRspDto.certificationFail();

        String password = reqDto.getPassword();
        String encodedPassword = passwordEncoder.encode(password);

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
                .role(Role.ROLE_USER)   // 일반 유저
                .build();

        userRepository.save(user);
        certificationRepository.delete(certification); // 회원가입이 되면 안증번호 내역 지우기

        return RegisterRspDto.success();
    }

    @Transactional
    public ResponseEntity<?> login(LogInReqDto reqDto, HttpServletResponse response){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(reqDto.getUsername(), reqDto.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = getUserByUsername(userDetails.getUsername());

            // 이용 정지 당한 경우
            if(user.getBanDate().isAfter(LocalDate.now())){
                return ResponseEntity.status(HttpStatus.LOCKED).build();
            }

            Role roles = user.getRole();

            String accessToken = jwtTokenizer.createAccessToken(
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getUsername(),
                    roles
            );

            String refreshToken = jwtTokenizer.createRefreshToken(
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getUsername(),
                    roles
            );

            Token tokenEntity = Token.builder()
                    .accessTokenValue(accessToken)
                    .refreshTokenValue(refreshToken)
                    .user(user)
                    .build();

            tokenService.saveToken(tokenEntity);

            LoginRspDto loginRspDto = LoginRspDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .username(userDetails.getUsername())
                    .build();

            // 쿠키 설정
            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT / 1000));

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.REFRESH_TOKEN_EXPIRE_COUNT / 1000));

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(loginRspDto);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

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

    @Transactional
    public ResponseEntity<?> getRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키로부터 refresh Token을 얻어온다.
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 없을 때 오류로 응답
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("refreshToken이 존재하지 않습니다.");
        }

        // 있을 때 토큰으로부터 정보를 얻어온다.
        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);
        String username = (String) claims.get("username");

        User user = getUserByUsername(username);

        // accessToken 생성
        Role roles = (Role) claims.get("roles");
        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), username, roles);

        // 쿠키 생성 response로 보내기
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(Math.toIntExact(JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT / 1000)); // 초 단위로 넘어오니까 밀리로 바꾸기 위해 1000으로 나눔.

        response.addCookie(accessTokenCookie);

        LoginRspDto loginRspDto = LoginRspDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .build();

        return ResponseEntity.ok(loginRspDto);
    }

    // 아이디 찾기
    @Transactional(readOnly = true)
    public ResponseEntity<String> findUserId(FindIdReqDto reqDto) {
        Optional<User> findUser = userRepository.findByNameAndEmail(reqDto.getName(), reqDto.getEmail());
        if (findUser.isPresent()) {
            return ResponseEntity.ok(findUser.get().getUsername());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionMessage.USER_NOT_FOUND.getMessage());
        }
    }

    // 비밀번호 찾기
    @Transactional
    public ResponseEntity<String> findPassword(FindPasswordReqDto reqDto) {
        Optional<User> user = userRepository.findByUsernameAndEmail(reqDto.getUsername(), reqDto.getEmail());

        if(user.isPresent()) {
            // 임시 비밀번호
            String tempPassword = TempPassword.certificationNumber();

            // 메일 전송
            tempPasswordEmailProvider.sendEmail(reqDto.getEmail(), tempPassword);

            // 해당 비밀번호로 유저 정보 수정
            String encodePassword = passwordEncoder.encode(tempPassword);
            user.get().updatePassword(encodePassword);

            return ResponseEntity.ok(userRepository.save(user.get()).getEmail() + "로 임시 비밀번호를 전송했습니다.");
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionMessage.USER_NOT_FOUND.getMessage());
        }
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
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(){
        User user = getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        userRepository.deleteById(user.getId());
    }
}