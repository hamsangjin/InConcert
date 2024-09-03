package com.inconcert.domain.user.service;

import com.inconcert.common.auth.CustomUserDetails;
import com.inconcert.common.auth.jwt.token.entity.Token;
import com.inconcert.common.auth.jwt.token.service.TokenService;
import com.inconcert.common.auth.jwt.util.JwtTokenizer;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.UserNotFoundException;
import com.inconcert.common.service.ImageService;
import com.inconcert.domain.certification.provider.EmailProvider;
import com.inconcert.domain.certification.provider.TempPasswordEmailProvider;
import com.inconcert.domain.certification.util.TempPassword;
import com.inconcert.domain.feedback.repository.FeedbackRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.dto.request.*;
import com.inconcert.domain.user.dto.response.*;
import com.inconcert.domain.user.entity.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.domain.certification.repository.CertificationRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.certification.entity.Certification;
import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private UserDetails userDetails;
    @Mock
    private JwtTokenizer jwtTokenizer;
    @Mock
    private TokenService tokenService;
    @Mock
    private ImageService imageService;
    @Mock
    private CertificationRepository certificationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailProvider emailProvider;
    @Mock
    private TempPasswordEmailProvider tempPasswordEmailProvider;

    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);

        // JwtTokenizer의 static 필드 초기화
        JwtTokenizer.ACCESS_TOKEN_EXPIRE_COUNT = 3600000L;
        JwtTokenizer.REFRESH_TOKEN_EXPIRE_COUNT = 86400000L;

        SecurityContextHolder.setContext(securityContext);
    }

    // 회원가입
    @Test
    void 회원가입() {
        // Given
        RegisterReqDto reqDto = new RegisterReqDto(
                "testuser",
                "password123!",
                "password123!",
                "test@example.com",
                "123456",
                "Test User",
                "testuser",
                "01012345678",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                Mbti.INTJ,
                true
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        Certification certification = new Certification(1L, "test@example.com", "123456", "testuser");

        when(certificationRepository.findByUsername("testuser")).thenReturn(certification);

        when(passwordEncoder.encode("password123!")).thenReturn("encodedPassword");

        // When
        ResponseEntity<? super RegisterRspDto> response = userService.joinUser(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(userRepository).save(any(User.class));
        verify(certificationRepository).delete(certification);
    }

    @Test
    void 중복_회원_실패() {
        // Given
        RegisterReqDto reqDto = new RegisterReqDto(
                "existinguser",
                "password123!",
                "password123!",
                "test@example.com",
                "123456",
                "Test User",
                "testuser",
                "01012345678",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                Mbti.INTJ,
                true
        );

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When
        ResponseEntity<? super RegisterRspDto> response = userService.joinUser(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void 비밀번호_불일치() {
        // Given
        RegisterReqDto reqDto = new RegisterReqDto(
                "testuser",
                "password123!",
                "differentpassword",
                "test@example.com",
                "123456",
                "Test User",
                "testuser",
                "01012345678",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                Mbti.INTJ,
                true
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        // When
        ResponseEntity<? super RegisterRspDto> response = userService.joinUser(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void 이메일_인증_실패() {
        // Given
        RegisterReqDto reqDto = new RegisterReqDto(
                "testuser",
                "password123!",
                "password123!",
                "test@example.com",
                "123456",
                "Test User",
                "testuser",
                "01012345678",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                Mbti.INTJ,
                true
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        Certification certification = new Certification(1L, "different@example.com", "654321", "testuser");

        when(certificationRepository.findByUsername("testuser")).thenReturn(certification);

        // When
        ResponseEntity<? super RegisterRspDto> response = userService.joinUser(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }


    @Test
    void 인증번호_전송_성공() {
        // Given
        EmailCertificationReqDto reqDto = new EmailCertificationReqDto("newuser", "test@example.com");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(emailProvider.sendEmail(anyString(), anyString())).thenReturn(true);

        // When
        ResponseEntity<? super EmailCertificationRspDto> responseEntity = userService.sendCertificationNumber(reqDto);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(certificationRepository).save(any(Certification.class));
    }

    @Test
    void 인증번호_전송_실패_중복_아이디() {
        // Given
        EmailCertificationReqDto reqDto = new EmailCertificationReqDto("existinguser", "test@example.com");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When
        ResponseEntity<? super EmailCertificationRspDto> responseEntity = userService.sendCertificationNumber(reqDto);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 인증번호_전송_실패_이메일_전송_오류() {
        // Given
        EmailCertificationReqDto reqDto = new EmailCertificationReqDto("newuser", "test@example.com");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(emailProvider.sendEmail(anyString(), anyString())).thenReturn(false);

        // When
        ResponseEntity<? super EmailCertificationRspDto> responseEntity = userService.sendCertificationNumber(reqDto);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void 아이디_중복_확인_성공() {
        // Given
        UsernameCheckReqDto reqDto = new UsernameCheckReqDto("newuser");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // When
        ResponseEntity<? super UsernameCheckRspDto> response = userService.checkUsername(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void 아이디_중복_확인_실패() {
        // Given
        UsernameCheckReqDto reqDto = new UsernameCheckReqDto("existinguser");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When
        ResponseEntity<? super UsernameCheckRspDto> response = userService.checkUsername(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void 이메일_중복_확인_성공() {
        // Given
        EmailCheckReqDto reqDto = new EmailCheckReqDto("new@example.com");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // When
        ResponseEntity<? super EmailCheckRspDto> response = userService.checkEmail(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void 이메일_중복_확인_실패() {
        // Given
        EmailCheckReqDto reqDto = new EmailCheckReqDto("existing@example.com");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When
        ResponseEntity<? super EmailCheckRspDto> response = userService.checkEmail(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void 닉네임_중복_확인_성공() {
        // Given
        NicknameCheckReqDto reqDto = new NicknameCheckReqDto("newnick");
        when(userRepository.existsByNickname("newnick")).thenReturn(false);

        // When
        ResponseEntity<? super NicknameCheckRspDto> response = userService.checkNickname(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void 닉네임_중복_확인_실패() {
        // Given
        NicknameCheckReqDto reqDto = new NicknameCheckReqDto("existingnick");
        when(userRepository.existsByNickname("existingnick")).thenReturn(true);

        // When
        ResponseEntity<? super NicknameCheckRspDto> response = userService.checkNickname(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void 전화번호_중복_확인_성공() {
        // Given
        PhoneNumberCheckReqDto reqDto = new PhoneNumberCheckReqDto("01012345678");
        when(userRepository.existsByPhoneNumber("01012345678")).thenReturn(false);

        // When
        ResponseEntity<? super PhoneNumberCheckRspDto> response = userService.checkPhoneNumber(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void 전화번호_중복_확인_실패() {
        // Given
        PhoneNumberCheckReqDto reqDto = new PhoneNumberCheckReqDto("01087654321");
        when(userRepository.existsByPhoneNumber("01087654321")).thenReturn(true);

        // When
        ResponseEntity<? super PhoneNumberCheckRspDto> response = userService.checkPhoneNumber(reqDto);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    // 로그인
    @Test
    void 로그인_성공() {
        // Given
        LogInReqDto reqDto = new LogInReqDto("testuser", "password123!");
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .name("Test User")
                .nickname("testnick")
                .phoneNumber("01012345678")
                .birth(LocalDate.of(1990, 1, 1))
                .profileImage("/images/profile.png")
                .gender(Gender.MALE)
                .mbti(Mbti.INTJ)
                .role(Role.ROLE_USER)
                .build();
        user.updateBanDate(LocalDate.now().minusDays(1));

        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getProfileImage(),
                List.of(user.getRole().name())
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));

        // JwtTokenizer 모의 객체 사용
        when(jwtTokenizer.createAccessToken(anyLong(), anyString(), anyString(), any(Role.class)))
                .thenReturn("access_token");
        when(jwtTokenizer.createRefreshToken(anyLong(), anyString(), anyString(), any(Role.class)))
                .thenReturn("refresh_token");

        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        ResponseEntity<?> responseEntity = userService.login(reqDto, response);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isInstanceOf(LoginRspDto.class);
        verify(tokenService).saveToken(any(Token.class));
        verify(response, times(2)).addCookie(any());
    }

    @Test
    void 로그인_실패_인증_오류() {
        // Given
        LogInReqDto reqDto = new LogInReqDto("testuser", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Authentication failed") {});

        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        ResponseEntity<?> responseEntity = userService.login(reqDto, response);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void 로그인_실패_이용정지_사용자() {
        // Given
        LogInReqDto reqDto = new LogInReqDto("banneduser", "password123!");
        User user = User.builder()
                .username("banneduser")
                .password("encodedPassword")
                .email("banned@example.com")
                .name("Banned User")
                .nickname("bannednick")
                .phoneNumber("01012345678")
                .birth(LocalDate.of(1990, 1, 1))
                .profileImage("/images/profile.png")
                .gender(Gender.MALE)
                .mbti(Mbti.INTJ)
                .role(Role.ROLE_USER)
                .build();
        // 사용자를 이용 정지 상태로 설정 (banDate를 현재 날짜 이후로 설정)
        user.updateBanDate(LocalDate.now().plusDays(7));  // 7일 후로 설정

        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getProfileImage(),
                List.of(user.getRole().name())
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByUsername("banneduser")).thenReturn(java.util.Optional.of(user));

        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        ResponseEntity<?> responseEntity = userService.login(reqDto, response);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        // 토큰 생성이나 쿠키 설정이 일어나지 않았는지 확인
        verify(jwtTokenizer, never()).createAccessToken(anyLong(), anyString(), anyString(), any(Role.class));
        verify(jwtTokenizer, never()).createRefreshToken(anyLong(), anyString(), anyString(), any(Role.class));
        verify(tokenService, never()).saveToken(any(Token.class));
        verify(response, never()).addCookie(any());
    }

    @Test
    void 사용자_찾음() {
        // Given
        String username = "testuser";
        User expectedUser = User.builder()
                .username(username)
                .email("test@example.com")
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        // When
        User result = userService.getUserByUsername(username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void 사용자_없음() {
        // Given
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("찾으려는 유저가 존재하지 않습니다.");
        verify(userRepository).findByUsername(username);
    }

    @Test
    void getRefreshToken_성공() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String refreshToken = "valid_refresh_token";
        request.setCookies(new Cookie("refreshToken", refreshToken));

        Claims claims = mock(Claims.class);
        when(claims.get("username")).thenReturn("testuser");
        when(claims.get("roles")).thenReturn(Role.ROLE_USER);
        when(jwtTokenizer.parseRefreshToken(refreshToken)).thenReturn(claims);

        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));

        String newAccessToken = "new_access_token";
        when(jwtTokenizer.createAccessToken(eq(user.getId()), eq(user.getEmail()), eq(user.getUsername()), eq(Role.ROLE_USER)))
                .thenReturn(newAccessToken);

        // When
        ResponseEntity<?> result = userService.getRefreshToken(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(LoginRspDto.class);
        LoginRspDto loginRspDto = (LoginRspDto) result.getBody();
        assertThat(loginRspDto.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(loginRspDto.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(loginRspDto.getUsername()).isEqualTo("testuser");

        assertThat(response.getCookies()).hasSize(1);
        assertThat(response.getCookies()[0].getName()).isEqualTo("accessToken");
        assertThat(response.getCookies()[0].getValue()).isEqualTo(newAccessToken);
    }

    @Test
    void getRefreshToken_쿠키_없음() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        ResponseEntity<?> result = userService.getRefreshToken(request, response);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo("refreshToken이 존재하지 않습니다.");
    }

    @Test
    void getRefreshToken_토큰파싱실패() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String invalidRefreshToken = "invalid_refresh_token";
        request.setCookies(new Cookie("refreshToken", invalidRefreshToken));

        when(jwtTokenizer.parseRefreshToken(invalidRefreshToken)).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        assertThatThrownBy(() -> userService.getRefreshToken(request, response))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void 아이디_찾기_성공() {
        // Given
        FindIdReqDto reqDto = FindIdReqDto.builder()
                .name("name")
                .email("user@user.1")
                .build();

        Optional<User> findUser = Optional.of(User.builder()
                .name(reqDto.getName())
                .email(reqDto.getEmail())
                .build());

        when(userRepository.findByNameAndEmail("name", "user@user.1")).thenReturn(findUser);

        // When
        ResponseEntity<String> result = userService.findUserId(reqDto);

        // Then
        assertThat(result.getBody()).isEqualTo(findUser.get().getUsername());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void 아이디_찾기_실패() {
        // Given
        FindIdReqDto reqDto = FindIdReqDto.builder()
                .name("name")
                .email("user@user.1")
                .build();
        given(userRepository.findByNameAndEmail(anyString(), anyString())).willReturn(Optional.empty());

        // When
        ResponseEntity<String> result = userService.findUserId(reqDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo(ExceptionMessage.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 비밀번호_찾기_성공() {
        // Given
        FindPasswordReqDto reqDto = FindPasswordReqDto.builder()
                .username("username")
                .email("user@user.1")
                .build();

        User findUser = User.builder()
                .name(reqDto.getUsername())
                .email(reqDto.getEmail())
                .build();

        Optional<User> optionalUser = Optional.of(findUser);

        String tempPassword = "tempPassword123";
        String encodedPassword = "encodedTempPassword123";


        // 정적 메서드 모킹(try-with-resources)
        try (MockedStatic<TempPassword> mockedTempPassword = mockStatic(TempPassword.class)) {
            mockedTempPassword.when(TempPassword::certificationNumber).thenReturn(tempPassword);    // 이 메소드가 정적 메소드여서 mockStatic으로 처리해야함
            given(passwordEncoder.encode(tempPassword)).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(findUser);
            given(userRepository.findByUsernameAndEmail("username", "user@user.1")).willReturn(optionalUser);

            // When
            ResponseEntity<String> result = userService.findPassword(reqDto);

            // Then
            assertThat(result.getBody()).isEqualTo(findUser.getEmail() + "로 임시 비밀번호를 전송했습니다.");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void 비밀번호_찾기_실패() {
        // Given
        FindPasswordReqDto reqDto = FindPasswordReqDto.builder()
                .username("username")
                .email("user@user.1")
                .build();
        given(userRepository.findByUsernameAndEmail(anyString(), anyString())).willReturn(Optional.empty());

        // When
        ResponseEntity<String> result = userService.findPassword(reqDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo(ExceptionMessage.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 인증된_유저_불러오기_성공() {
        // Given
        String username = "testUser";
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(userDetails.getUsername()).willReturn(username);

        User user = User.builder()
                .username(username)
                .build();
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        // When
        Optional<User> result = userService.getAuthenticatedUser();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
    }

    @Test
    void 인증된_유저_불러오기_실패(){
        // Given
        given(securityContext.getAuthentication()).willThrow(new RuntimeException("Authentication failed"));

        // When
        Optional<User> result = userService.getAuthenticatedUser();

        // Then
        assertThat(result).isNotPresent();
    }

    @Test
    void 유저_업데이트() {
        // Given
        User user = new User(1L, "user1", "user@user.1");
        given(userRepository.save(any(User.class))).willReturn(user);

        // When
        userService.updateUser(user);

        // Then
        verify(userRepository).save(user);
    }

    @Test
    void 유저_삭제() {
        // Given
        User user = new User(1L, "user1", "user@user.1", new HashSet<>());

        // SecurityContext에 Authentication 객체 설정
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn("user1");
        given(userService.getAuthenticatedUser()).willReturn(Optional.of(user));

        // 아무 동작 안 하게 설정(정상적으로 작동 시 void이기 때문)
        doNothing().when(userRepository).deleteById(anyLong());

        // When
        userService.deleteUser();

        // Then
        verify(userRepository).deleteById(user.getId());
    }
}