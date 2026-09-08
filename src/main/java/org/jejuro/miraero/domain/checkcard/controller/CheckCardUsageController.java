package org.jejuro.miraero.domain.checkcard.controller;

import java.time.YearMonth;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.checkcard.dto.response.CheckCardUsageExecutionResponse;
import org.jejuro.miraero.domain.checkcard.dto.response.CheckCardUsageStatusResponse;
import org.jejuro.miraero.domain.checkcard.service.CheckCardUsageService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check-card-usage")
@RequiredArgsConstructor
@Api(tags = "체크카드 사용 실적")
public class CheckCardUsageController {
    private final CheckCardUsageService checkCardUsageService;

    @GetMapping
    @ApiOperation(
            value = "체크카드 사용 실적 상위 2개 카드 조회",
            notes = "로그인 사용자가 보유한 체크카드 중 연속 충족 개월 수가 높은 상위 2개 카드의 진행 상태를 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<CheckCardUsageStatusResponse>>> getStatus(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        List<CheckCardUsageStatusResponse> response = checkCardUsageService.getStatus(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/execute")
    @ApiOperation(
            value = "체크카드 사용 실적 판정 즉시 실행",
            notes = "로그인 사용자에 대해 지정한 월 기준으로 체크카드 사용 실적 판정을 즉시 실행합니다. "
                    + "month를 생략하면 지난달을 기준으로 실행하며, 아직 끝나지 않은 달은 지정할 수 없습니다."
    )
    public ResponseEntity<ApiResponse<CheckCardUsageExecutionResponse>> execute(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ApiParam(value = "판정 기준월(yyyy-MM). 생략 시 지난달", example = "2026-08")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        YearMonth targetMonth = month == null ? YearMonth.now().minusMonths(1) : month;

        // 아직 끝나지 않은 달(이번 달 이후)은 판정 대상이 될 수 없음 — 그 달의 거래가 아직 다 안 쌓였으니까
        if (!targetMonth.isBefore(YearMonth.now())) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        int processedCount = checkCardUsageService.executeAll(targetMonth, user.getUserId());

        return ResponseEntity.ok(ApiResponse.success(
                CheckCardUsageExecutionResponse.builder()
                        .targetMonth(targetMonth)
                        .processedCount(processedCount)
                        .build()
        ));
    }
}
