package org.jejuro.miraero.domain.loansimulation.controller;

import java.time.LocalDate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.loansimulation.dto.response.LoanDelinquencyExecutionResponse;
import org.jejuro.miraero.domain.loansimulation.service.LoanDelinquencyExecutionService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loan-simulations")
@RequiredArgsConstructor
@Api(tags = "대출 시뮬레이션")
public class LoanDelinquencyController {

    private final LoanDelinquencyExecutionService loanDelinquencyExecutionService;

    @PostMapping("/execute")
    @ApiOperation(
            value = "대출 시뮬레이션 즉시 실행",
            notes = "로그인 사용자의 목표저축에 대해 연체/연속상환 판정을 지정한 날짜 기준으로 즉시 실행합니다. "
                    + "date를 생략하면 서버의 오늘 날짜를 사용하며, 미래 날짜는 지정할 수 없습니다."
    )
    public ResponseEntity<ApiResponse<LoanDelinquencyExecutionResponse>> execute(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ApiParam(value = "실행 기준일(yyyy-MM-dd). 생략 시 오늘", example = "2026-09-06")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate executionDate = date == null ? LocalDate.now() : date;

        if (executionDate.isAfter(LocalDate.now())) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        int processedCount = loanDelinquencyExecutionService.executeAll(executionDate, user.getUserId());

        return ResponseEntity.ok(ApiResponse.success(
                LoanDelinquencyExecutionResponse.builder()
                        .executionDate(executionDate)
                        .processedCount(processedCount)
                        .build()
        ));
    }
}
