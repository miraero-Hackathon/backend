package org.jejuro.miraero.domain.auth.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.auth.dto.request.LoginRequest;
import org.jejuro.miraero.domain.auth.dto.request.SignUpRequest;
import org.jejuro.miraero.domain.auth.dto.response.LoginResponse;
import org.jejuro.miraero.domain.auth.dto.response.SignUpResponse;
import org.jejuro.miraero.domain.auth.dto.response.TokenReissueResponse;
import org.jejuro.miraero.domain.auth.service.AuthService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Api(tags = "인증")
public class AuthController {

  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
  private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth";
  private static final String SAME_SITE_POLICY = "Lax";

  private final AuthService authService;

  @PostMapping("/signup")
  @ApiOperation(value = "회원가입", notes = "이메일과 비밀번호로 회원가입합니다. 이메일은 중복될 수 없으며, 비밀번호는 8자 이상이어야 합니다.")
  public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
      @Valid @RequestBody SignUpRequest request
  ) {
    SignUpResponse response = authService.signUp(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success(response));
  }

  @PostMapping("/login")
  @ApiOperation(value = "로그인", notes = "응답 본문의 token.accessToken을 이후 인증 API의 Authorization 헤더에 `Bearer {accessToken}` 형식으로 사용합니다. Refresh Token은 응답 본문에 포함되지 않고 HttpOnly 쿠키로 설정됩니다.")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletResponse httpServletResponse
  ) {
    LoginResponse response = authService.login(request);

    addRefreshTokenCookie(
        httpServletResponse,
        response.getRefreshToken(),
        response.getRefreshTokenExpiresIn()
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/reissue")
  @ApiOperation(value = "Access Token 재발급", notes = "로그인 시 설정된 refreshToken HttpOnly 쿠키로 Access Token을 재발급합니다. 프론트는 쿠키를 직접 전달하거나 읽지 않으며, 요청 시 쿠키 포함 설정(withCredentials)이 필요할 수 있습니다.")
  public ResponseEntity<ApiResponse<TokenReissueResponse>> reissue(
      @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse httpServletResponse
  ) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    TokenReissueResponse response = authService.reissue(refreshToken);

    addRefreshTokenCookie(
        httpServletResponse,
        response.getRefreshToken(),
        response.getRefreshTokenExpiresIn()
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/logout")
  @ApiOperation(value = "로그아웃", notes = "인증된 사용자의 Refresh Token을 무효화하고 refreshToken 쿠키를 만료시킵니다. 성공 시 응답 본문 없이 204 No Content를 반환합니다.")
  public ResponseEntity<Void> logout(
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
      HttpServletResponse httpServletResponse
  ) {
    authService.logout(authenticatedUser.getUserId());

    expireRefreshTokenCookie(httpServletResponse);

    return ResponseEntity.noContent().build();
  }

  private void addRefreshTokenCookie(
      HttpServletResponse httpServletResponse,
      String refreshToken,
      Long refreshTokenExpiresIn
  ) {
    ResponseCookie refreshTokenCookie = ResponseCookie.from(
            REFRESH_TOKEN_COOKIE_NAME,
            refreshToken
        )
        .httpOnly(true)
        .secure(false)
        .path(REFRESH_TOKEN_COOKIE_PATH)
        .maxAge(refreshTokenExpiresIn)
        .sameSite(SAME_SITE_POLICY)
        .build();

    httpServletResponse.addHeader(
        HttpHeaders.SET_COOKIE,
        refreshTokenCookie.toString()
    );
  }

  private void expireRefreshTokenCookie(
      HttpServletResponse httpServletResponse
  ) {
    ResponseCookie refreshTokenCookie = ResponseCookie.from(
            REFRESH_TOKEN_COOKIE_NAME,
            ""
        )
        .httpOnly(true)
        .secure(false)
        .path(REFRESH_TOKEN_COOKIE_PATH)
        .maxAge(0)
        .sameSite(SAME_SITE_POLICY)
        .build();

    httpServletResponse.addHeader(
        HttpHeaders.SET_COOKIE,
        refreshTokenCookie.toString()
    );
  }

}
