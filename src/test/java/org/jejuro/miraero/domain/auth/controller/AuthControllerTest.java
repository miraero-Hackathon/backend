package org.jejuro.miraero.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.jejuro.miraero.domain.auth.dto.request.LoginRequest;
import org.jejuro.miraero.domain.auth.dto.request.SignUpRequest;
import org.jejuro.miraero.domain.auth.dto.response.LoginResponse;
import org.jejuro.miraero.domain.auth.dto.response.LoginUserResponse;
import org.jejuro.miraero.domain.auth.dto.response.SignUpResponse;
import org.jejuro.miraero.domain.auth.dto.response.TokenReissueResponse;
import org.jejuro.miraero.domain.auth.service.AuthService;
import org.jejuro.miraero.domain.user.exception.UserErrorCode;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private AuthService authService;

  @BeforeEach
  void setUp() {
    AuthController authController = new AuthController(authService);

    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders
        .standaloneSetup(authController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setValidator(validator)
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .build();

    objectMapper = new ObjectMapper();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("회원가입 요청이 유효하면 201 상태 코드와 회원 정보를 반환한다")
  void signUp_success() throws Exception {
    SignUpRequest request = new SignUpRequest(
        "test@example.com",
        "password123!"
    );

    SignUpResponse response = new SignUpResponse(
        1L,
        "테스트 사용자",
        "test@example.com"
    );

    given(authService.signUp(any(SignUpRequest.class)))
        .willReturn(response);

    mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value(1))
        .andExpect(jsonPath("$.data.name").value("테스트 사용자"))
        .andExpect(jsonPath("$.data.email").value("test@example.com"));

    verify(authService).signUp(any(SignUpRequest.class));
  }

  @Test
  @DisplayName("회원가입 이메일 형식이 올바르지 않으면 400 상태 코드를 반환한다")
  void signUp_invalidEmail() throws Exception {
    SignUpRequest request = new SignUpRequest(
        "invalid-email",
        "password123!"
    );

    mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_002"));

    verify(authService, never()).signUp(any(SignUpRequest.class));
  }

  @Test
  @DisplayName("회원가입 이메일이 비어 있으면 400 상태 코드를 반환한다")
  void signUp_blankEmail() throws Exception {
    SignUpRequest request = new SignUpRequest(
        "",
        "password123!"
    );

    mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_002"));

    verify(authService, never()).signUp(any(SignUpRequest.class));
  }

  @Test
  @DisplayName("이미 가입된 이메일이면 409 상태 코드와 USER_001을 반환한다")
  void signUp_duplicateEmail() throws Exception {
    SignUpRequest request = new SignUpRequest(
        "test@example.com",
        "password123!"
    );

    given(authService.signUp(any(SignUpRequest.class)))
        .willThrow(new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS));

    mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("USER_001"));

    verify(authService).signUp(any(SignUpRequest.class));
  }

  @Test
  @DisplayName("로그인 요청이 유효하면 200 상태 코드와 로그인 정보를 반환한다")
  void login_success() throws Exception {
    LoginRequest request = new LoginRequest(
        "test@example.com",
        "password123!"
    );

    LoginResponse response = new LoginResponse(
        "access-token",
        "refresh-token",
        1800L,
        1209600L,
        true,
        new LoginUserResponse(
            1L,
            "테스트 사용자",
            "test@example.com",
            true,
            true
        )
    );

    given(authService.login(any(LoginRequest.class)))
        .willReturn(response);

    mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(header().string(
            HttpHeaders.SET_COOKIE,
            Matchers.containsString("refreshToken=refresh-token")
        ))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.token.accessToken").value("access-token"))
        .andExpect(jsonPath("$.data.token.refreshToken").doesNotExist())
        .andExpect(jsonPath("$.data.token.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.data.token.accessTokenExpiresIn").value(1800))
        .andExpect(jsonPath("$.data.token.refreshTokenExpiresIn").doesNotExist())
        .andExpect(jsonPath("$.data.autoLogin").value(true))
        .andExpect(jsonPath("$.data.user.userId").value(1))
        .andExpect(jsonPath("$.data.user.name").value("테스트 사용자"))
        .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
        .andExpect(jsonPath("$.data.user.mydataConnected").value(true))
        .andExpect(jsonPath("$.data.user.goalSet").value(true));

    verify(authService).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("Refresh Token 쿠키가 있으면 Access Token을 재발급한다")
  void reissue_usesRefreshTokenCookieAndReturnsAccessToken() throws Exception {
    TokenReissueResponse response = new TokenReissueResponse(
        "new-access-token",
        "new-refresh-token",
        1800L,
        1209600L,
        new LoginUserResponse(1L, "테스트 사용자", "test@example.com", true, true)
    );

    given(authService.reissue("refresh-token"))
        .willReturn(response);

    mockMvc.perform(
            post("/api/auth/reissue")
                .cookie(new Cookie("refreshToken", "refresh-token"))
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(header().string(
            HttpHeaders.SET_COOKIE,
            Matchers.containsString("refreshToken=new-refresh-token")
        ))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.token.accessToken").value("new-access-token"))
        .andExpect(jsonPath("$.data.token.refreshToken").doesNotExist())
        .andExpect(jsonPath("$.data.refreshToken").doesNotExist());

    verify(authService).reissue("refresh-token");
  }

  @Test
  @DisplayName("Refresh Token 쿠키가 없으면 400 상태 코드를 반환한다")
  void reissue_withoutRefreshTokenCookieReturnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/auth/reissue"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_002"));

    verify(authService, never()).reissue(any());
  }

  @Test
  @DisplayName("로그인 이메일이 비어 있으면 400 상태 코드를 반환한다")
  void login_blankEmail() throws Exception {
    LoginRequest request = new LoginRequest(
        "",
        "password123!"
    );

    mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_002"));

    verify(authService, never()).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("로그인 이메일 형식이 올바르지 않으면 400 상태 코드를 반환한다")
  void login_invalidEmail() throws Exception {
    LoginRequest request = new LoginRequest(
        "invalid-email",
        "password123!"
    );

    mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_002"));

    verify(authService, never()).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("로그인 비밀번호가 비어 있으면 400 상태 코드를 반환한다")
  void login_blankPassword() throws Exception {
    LoginRequest request = new LoginRequest(
        "test@example.com",
        ""
    );

    mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("COMMON_002"));

    verify(authService, never()).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("로그아웃하면 Refresh Token 쿠키를 만료시키고 204 상태 코드를 반환한다")
  void logout_success() throws Exception {
    Long userId = 1L;

    SecurityContextHolder.getContext().setAuthentication(
        new JwtAuthenticationToken(new AuthenticatedUser(userId))
    );

    mockMvc.perform(post("/api/auth/logout"))
        .andDo(print())
        .andExpect(status().isNoContent())
        .andExpect(header().string(
            HttpHeaders.SET_COOKIE,
            Matchers.allOf(
                Matchers.containsString("refreshToken="),
                Matchers.containsString("Max-Age=0"),
                Matchers.containsString("HttpOnly")
            )
        ));

    verify(authService).logout(userId);
  }
}
