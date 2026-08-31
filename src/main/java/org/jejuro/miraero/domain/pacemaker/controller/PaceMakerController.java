package org.jejuro.miraero.domain.pacemaker.controller;

import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerCreateRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerGoalDepositRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerHistorySearchCondition;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerMaxAmountUpdateRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerStatusUpdateRequest;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerCreateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalListResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerHistoryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMaxAmountUpdateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.service.PaceMakerService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.response.PageResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDate;
import org.jejuro.miraero.domain.autotransfer.exception.AutoTransferErrorCode;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerSavingExecutionResponse;
import org.jejuro.miraero.domain.pacemaker.service.PaceMakerSavingService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pace-maker")
@Api(tags = "페이스메이커")
public class PaceMakerController {

  private final PaceMakerService paceMakerService;
  private final PaceMakerSavingService paceMakerSavingService;

  @PostMapping
  @ApiOperation(
      value = "페이스메이커 개설",
      notes = "적립용 저금통과 자동 적립 설정을 함께 만듭니다. 사용자당 하나만 개설할 수 있습니다."
  )
  public ResponseEntity<ApiResponse<PaceMakerCreateResponse>> createPaceMaker(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @RequestBody PaceMakerCreateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(paceMakerService.createPaceMaker(user.getUserId(), request)));
  }

  @GetMapping
  @ApiOperation(value = "페이스메이커 조회", notes = "로그인 사용자의 자동 저축 페이스메이커 설정을 조회합니다.")
  public ResponseEntity<ApiResponse<PaceMakerResponse>> getPaceMaker(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerResponse response = paceMakerService.getPaceMaker(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping("/{autoSavingId}/status")
  @ApiOperation(value = "페이스메이커 상태 변경", notes = "자동 저축 페이스메이커의 활성 상태를 변경합니다.")
  public ResponseEntity<ApiResponse<PaceMakerResponse>> updatePaceMaker(
      @ApiParam(value = "자동 저축 ID", example = "1", required = true) @PathVariable Long autoSavingId,
      @Valid @RequestBody PaceMakerStatusUpdateRequest request,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerResponse response = paceMakerService.updateStatus(
        user.getUserId(),
        autoSavingId,
        request.getStatus()
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/dashboard")
  @ApiOperation(value = "페이스메이커 대시보드 조회", notes = "오늘의 저축, 머니박스 잔액, 월간 성공 현황을 조회합니다. includeStreak=true이면 연속 저축 기록도 포함합니다.")
  public ResponseEntity<ApiResponse<PaceMakerDashboardResponse>> getDashboard(
      @AuthenticationPrincipal AuthenticatedUser user,
      @ApiParam(value = "연속 저축 기록 포함 여부", example = "false") @RequestParam(defaultValue = "false") boolean includeStreak
  ) {
    PaceMakerDashboardResponse response =
        paceMakerService.getDashboard(user.getUserId(), includeStreak);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PatchMapping("/{autoSavingId}/max-amount")
  @ApiOperation(value = "페이스메이커 최대 저축 금액 변경", notes = "자동 저축 1회 최대 금액을 변경합니다.")
  public ResponseEntity<ApiResponse<PaceMakerMaxAmountUpdateResponse>> updateMaxAmount(
      @ApiParam(value = "자동 저축 ID", example = "1", required = true) @PathVariable Long autoSavingId,
      @Valid @RequestBody PaceMakerMaxAmountUpdateRequest request,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerMaxAmountUpdateResponse response = paceMakerService.updateMaxAmount(
        user.getUserId(),
        autoSavingId,
        request.getMaxAmount()
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/histories")
  @ApiOperation(value = "페이스메이커 저축 이력 조회", notes = "자동 저축 이력을 조건과 페이지 기준으로 조회합니다.")
  public ResponseEntity<ApiResponse<PageResponse<PaceMakerHistoryResponse>>> getHistories(
      @ModelAttribute PaceMakerHistorySearchCondition condition,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PageResponse<PaceMakerHistoryResponse> response = paceMakerService.getHistories(
        user.getUserId(),
        condition
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/goals")
  @ApiOperation(value = "페이스메이커 연결 목표 조회", notes = "페이스메이커로 저축할 수 있는 목표 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<PaceMakerGoalListResponse>> getPaceMakerGoals(
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerGoalListResponse response = paceMakerService.getPaceMakerGoals(user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/deposits")
  @ApiOperation(
      value = "목표에 저축금 입금",
      notes = "페이스메이커를 통해 선택한 목표에 저축금을 입금합니다. "
          + "목표 연결 자산이 예적금(ACCOUNT)이든 저금통(MONEY_BOX)이든 입금할 수 있습니다."
  )
  public ResponseEntity<ApiResponse<PaceMakerGoalDepositResponse>> depositToGoal(
      @Valid @RequestBody PaceMakerGoalDepositRequest request,
      @AuthenticationPrincipal AuthenticatedUser user
  ) {
    PaceMakerGoalDepositResponse response = paceMakerService.depositToGoal(
        user.getUserId(),
        request
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 페이스메이커 적립을 즉시 실행한다.
   *
   * 스케줄러는 매일 08:00에만 돌아 시연 중에 보여줄 수 없어서 둔 엔드포인트다.
   * date를 주면 그날 구간을 정산해 지난 날짜들을 순서대로 재생할 수 있다.
   * 본인 페이스메이커만 대상이라 다른 사용자의 데이터에는 영향이 없다.
   */
  @PostMapping("/savings/execute")
  @ApiOperation(
      value = "페이스메이커 적립 즉시 실행",
      notes = "시연·검증용입니다. 지정한 영업일(08:00~다음날 08:00)에 쓰고 남은 여유자금을 저금통에 적립합니다."
  )
  public ResponseEntity<ApiResponse<PaceMakerSavingExecutionResponse>> executeSaving(
      @AuthenticationPrincipal AuthenticatedUser user,
      @ApiParam(value = "정산 기준 영업일(yyyy-MM-dd). 생략 시 어제", example = "2026-08-11")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    // 기본값은 어제. 오늘 구간은 아직 진행 중이라 정산 대상이 아니다.
    LocalDate businessDate = date == null ? LocalDate.now().minusDays(1) : date;

    if (businessDate.isAfter(LocalDate.now())) {
      throw new BusinessException(AutoTransferErrorCode.FUTURE_EXECUTION_DATE);
    }

    int savedCount = paceMakerSavingService.saveAll(businessDate, user.getUserId());

    return ResponseEntity.ok(ApiResponse.success(
        PaceMakerSavingExecutionResponse.builder()
            .businessDate(businessDate)
            .savedCount(savedCount)
            .build()
    ));
  }
}
