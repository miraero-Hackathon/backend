package org.jejuro.miraero.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.jejuro.miraero.domain.auth.dto.request.LoginRequest;
import org.jejuro.miraero.domain.auth.dto.request.SignUpRequest;
import org.jejuro.miraero.domain.auth.dto.response.LoginResponse;
import org.jejuro.miraero.domain.auth.dto.response.SignUpResponse;
import org.jejuro.miraero.domain.auth.exception.AuthErrorCode;
import org.jejuro.miraero.domain.auth.repository.RefreshTokenRepository;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.domain.user.service.UserCreateCommand;
import org.jejuro.miraero.domain.user.service.UserService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.security.AuthTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthTokenProvider authTokenProvider;

  @Mock
  private UserService userService;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private GoalMapper goalMapper;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthServiceImpl(
        userMapper,
        passwordEncoder,
        authTokenProvider,
        userService,
        refreshTokenRepository,
        goalMapper
    );
  }

  @Test
  @DisplayName("회원가입 요청을 UserService에 위임하고 가입 응답을 반환한다")
  void signUp_success() {
    SignUpRequest request = new SignUpRequest(
        "test@example.com",
        "password123!"
    );

    User user = createUser();

    when(userService.create(any(UserCreateCommand.class)))
        .thenReturn(user);

    SignUpResponse response = authService.signUp(request);

    assertEquals(user.getUserId(), response.getUserId());
    assertEquals(user.getName(), response.getName());
    assertEquals(user.getEmail(), response.getEmail());

    verify(userService).create(any(UserCreateCommand.class));
  }

  @Test
  @DisplayName("이메일과 비밀번호가 일치하면 로그인 응답을 반환한다")
  void login_success() {
    LoginRequest request = new LoginRequest(
        "test@example.com",
        "password123!"
    );

    User user = createUser();

    when(userMapper.findByEmail(request.getEmail()))
        .thenReturn(user);
    when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
        .thenReturn(true);
    when(authTokenProvider.createAccessToken(user.getUserId()))
        .thenReturn("access-token");
    when(authTokenProvider.createRefreshToken(user.getUserId()))
        .thenReturn("refresh-token");
    when(authTokenProvider.getAccessTokenExpiresIn())
        .thenReturn(1800L);
    when(authTokenProvider.getRefreshTokenExpiresIn())
        .thenReturn(1209600L);
    when(goalMapper.existsActiveGoalByUserId(user.getUserId()))
        .thenReturn(true);

    LoginResponse response = authService.login(request);

    assertEquals("access-token", response.getToken().getAccessToken());
    assertEquals("Bearer", response.getToken().getTokenType());
    assertEquals(1800L, response.getToken().getAccessTokenExpiresIn());
    assertEquals("refresh-token", response.getRefreshToken());
    assertEquals(1209600L, response.getRefreshTokenExpiresIn());
    assertEquals(true, response.getAutoLogin());
    assertEquals(user.getUserId(), response.getUser().getUserId());
    assertEquals(user.getName(), response.getUser().getName());
    assertEquals(user.getEmail(), response.getUser().getEmail());
    assertEquals(true, response.getUser().getMydataConnected());
    assertEquals(true, response.getUser().getGoalSet());

    verify(userMapper).findByEmail("test@example.com");
    verify(passwordEncoder).matches("password123!", "encodedPassword");
    verify(authTokenProvider).createAccessToken(user.getUserId());
    verify(authTokenProvider).createRefreshToken(user.getUserId());
  }

  @Test
  @DisplayName("이메일에 해당하는 회원이 없으면 인증 예외를 던진다")
  void login_emailNotFound() {
    LoginRequest request = new LoginRequest(
        "not-found@example.com",
        "password123!"
    );

    when(userMapper.findByEmail(request.getEmail()))
        .thenReturn(null);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> authService.login(request)
    );

    assertEquals(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD, exception.getErrorCode());

    verify(userMapper).findByEmail("not-found@example.com");
    verify(passwordEncoder, never()).matches(any(), any());
    verify(authTokenProvider, never()).createAccessToken(any());
    verify(authTokenProvider, never()).createRefreshToken(any());
  }

  @Test
  @DisplayName("비밀번호가 일치하지 않으면 인증 예외를 던진다")
  void login_passwordMismatch() {
    LoginRequest request = new LoginRequest(
        "test@example.com",
        "wrongPassword123!"
    );

    User user = createUser();

    when(userMapper.findByEmail(request.getEmail()))
        .thenReturn(user);
    when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
        .thenReturn(false);

    BusinessException exception = assertThrows(
        BusinessException.class,
        () -> authService.login(request)
    );

    assertEquals(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD, exception.getErrorCode());

    verify(userMapper).findByEmail("test@example.com");
    verify(passwordEncoder).matches("wrongPassword123!", "encodedPassword");
    verify(authTokenProvider, never()).createAccessToken(any());
    verify(authTokenProvider, never()).createRefreshToken(any());
    verify(refreshTokenRepository, never()).save(any(), any(), any());
  }

  @Test
  @DisplayName("로그아웃하면 사용자의 Refresh Token을 삭제한다.")
  void logout_success() {
    Long userId = 1L;

    authService.logout(userId);
    verify(refreshTokenRepository).deleteByUserId(userId);
  }

  private User createUser() {
    return User.create(
        "테스트 사용자",
        LocalDate.of(2000, 1, 1),
        "테스트 회사",
        3_000_000L,
        "test@example.com",
        "encodedPassword",
        1L
    );
  }
}
