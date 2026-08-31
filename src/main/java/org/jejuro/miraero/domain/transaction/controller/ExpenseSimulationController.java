package org.jejuro.miraero.domain.transaction.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseSimulationRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseSimulationResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseSimulationService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expense-analysis")
@Api(tags = "지출 분석")
public class ExpenseSimulationController {

    private final ExpenseSimulationService expenseSimulationService;

    @PostMapping("/simulation")
    @ApiOperation(value = "목표 지출 시뮬레이션", notes = "선택한 월의 현재 지출과 카테고리별 목표 지출을 비교하여 절감 필요 금액을 반환합니다.")
    public ResponseEntity<ApiResponse<ExpenseSimulationResponse>> simulate(
            @Valid @RequestBody ExpenseSimulationRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        ExpenseSimulationResponse response = expenseSimulationService.simulate(user.getUserId(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
