package org.jejuro.miraero.domain.mydata.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.mydata.dto.MyDataConnectionListResponse;
import org.jejuro.miraero.domain.mydata.dto.response.MyDataConnectResponse;
import org.jejuro.miraero.domain.mydata.service.MyDataConnectService;
import org.jejuro.miraero.domain.mydata.service.MyDataConsentService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/mydata")
@RequiredArgsConstructor
@Api(tags = "마이데이터")
public class MyDataController {

  private final MyDataConsentService myDataConsentService;
  private final MyDataConnectService myDataConnectService;

  @GetMapping("/connections")
  @ApiOperation(value = "마이데이터 연결 상태 조회", notes = "로그인 사용자의 금융기관별 마이데이터 연결·동의·동기화 상태를 조회합니다.")
  public ResponseEntity<ApiResponse<MyDataConnectionListResponse>> getConnections(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    MyDataConnectionListResponse response =
        myDataConsentService.getConnections(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/connect")
  @ApiOperation(value = "마이데이터 연결", notes = "마이데이터 제공기관에 사용자를 연결하고 연동에 필요한 토큰을 저장합니다. 현재 구현은 모의 제공기관을 사용합니다.")
  public ResponseEntity<ApiResponse<MyDataConnectResponse>> connect(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    MyDataConnectResponse response = myDataConnectService.connect(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/sync")
  @ApiOperation(value = "마이데이터 동기화", notes = "연결된 마이데이터의 계좌와 거래 정보를 최신 상태로 동기화합니다. 아직 연결하지 않은 사용자는 호출할 수 없습니다.")
  public ResponseEntity<ApiResponse<Void>> sync(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    myDataConnectService.sync(user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
