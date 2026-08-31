package org.jejuro.miraero.domain.account.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.service.AccountService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Api(tags = "계좌")
public class AccountController {

  private final AccountService accountService;

  @GetMapping
  @ApiOperation(value = "내 계좌 목록 조회", notes = "로그인 사용자의 계좌 목록과 목록에 포함된 계좌 잔액의 합계를 조회합니다. accountType으로 계좌 유형을 필터링할 수 있습니다.")
  public ResponseEntity<ApiResponse<AccountListResponse>> getAccounts(
      @AuthenticationPrincipal AuthenticatedUser user,
      @ApiParam(value = "계좌 유형 필터. CHECKING, SAVINGS, DEPOSIT, INSTALLMENT, ISA, CMA 중 하나", example = "SAVINGS") @RequestParam(required = false) String accountType,
      @ApiParam(value = "true면 이미 목표에 연결된 계좌를 제외합니다. 끌어쓰기 출처 계좌를 고를 때 사용합니다.", example = "false") @RequestParam(defaultValue = "false") boolean excludeGoalLinked
  ) {
    AccountListResponse response =
        accountService.getAccounts(user.getUserId(), accountType, excludeGoalLinked);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/{accountId}")
  @ApiOperation(value = "내 계좌 상세 조회", notes = "로그인 사용자가 소유한 계좌의 상세 정보를 조회합니다. 다른 사용자의 계좌 또는 없는 계좌는 조회할 수 없습니다.")
  public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
      @ApiParam(value = "계좌 ID", example = "1", required = true) @PathVariable Long accountId,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    AccountResponse response = accountService.getAccount(accountId, user.getUserId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
