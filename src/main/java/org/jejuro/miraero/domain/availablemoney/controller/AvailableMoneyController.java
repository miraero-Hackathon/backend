package org.jejuro.miraero.domain.availablemoney.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.availablemoney.dto.response.DailyAvailableMoneyResponse;
import org.jejuro.miraero.domain.availablemoney.dto.response.MonthlyAvailableMoneyResponse;
import org.jejuro.miraero.domain.availablemoney.service.AvailableMoneyService;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals/{goalId}/available-money")
@Api(tags = "목표 - 가용 금액")
public class AvailableMoneyController {

    private final AvailableMoneyService availableMoneyService;

    /**
     * 월 여유자금 조회
     */
    @GetMapping("/monthly")
    @ApiOperation(value = "목표 월간 가용 금액 조회", notes = "목표 달성을 위해 현재 월에 사용할 수 있는 가용 금액을 조회합니다.")
    public ResponseEntity<MonthlyAvailableMoneyResponse> getMonthlyAvailableMoney(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable("goalId") Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user.getUserId();
        MonthlyAvailableMoneyResponse response = availableMoneyService.getMonthlyAvailableMoney(userId, goalId);
        return ResponseEntity.ok(response);
    }

    /**
     * 일일 여유자금 및 오늘 남은 자금 조회
     */
    @GetMapping("/daily")
    @ApiOperation(value = "목표 일일 가용 금액 조회", notes = "목표 달성을 위해 오늘 사용할 수 있는 가용 금액과 일일 권장 금액을 조회합니다.")
    public ResponseEntity<DailyAvailableMoneyResponse> getDailyAvailableMoney(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable("goalId") Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user.getUserId();
        DailyAvailableMoneyResponse response = availableMoneyService.getDailyAvailableMoney(userId, goalId);
        return ResponseEntity.ok(response);
    }
}
