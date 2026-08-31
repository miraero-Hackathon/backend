package org.jejuro.miraero.domain.user.controller;

import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.user.dto.request.PasswordChangeRequest;
import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;
import org.jejuro.miraero.domain.user.service.UserService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Api(tags = "사용자")
public class UserController {

  private final UserService userService;

  @GetMapping("/profile")
  @ApiOperation(value = "내 프로필 조회", notes = "로그인 사용자의 프로필과 서비스 설정 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    ProfileResponse response = userService.getProfile(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping("/me/password")
  @ApiOperation(value = "비밀번호 변경", notes = "현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다.")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @Valid @RequestBody PasswordChangeRequest request,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    userService.changePassword(user.getUserId(), request);

    return ResponseEntity.ok(ApiResponse.success(null));
  }
}

