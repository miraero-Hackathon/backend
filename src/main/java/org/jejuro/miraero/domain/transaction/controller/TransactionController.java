package org.jejuro.miraero.domain.transaction.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.TransactionResponse;
import org.jejuro.miraero.domain.transaction.service.TransactionService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.response.PageResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
@Api(tags = "거래 내역")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    @ApiOperation(value = "거래 내역 조회", notes = "로그인 사용자의 거래 내역을 월·카테고리 기준으로 조회합니다. page는 1부터 시작합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year", value = "조회 연도 (예: 2026)", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "month", value = "조회 월 (1~12)", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "categoryId", value = "지출 카테고리 ID", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "페이지 번호. 1부터 시작", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "페이지당 항목 수", dataType = "int", paramType = "query")
    })
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @ModelAttribute TransactionSearchCondition condition,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        PageResponse<TransactionResponse> response = transactionService.getTransactions(user.getUserId(), condition);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
