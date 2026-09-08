package org.jejuro.miraero.domain.credit.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.credit.dto.request.CreditScoreEventSearchCondition;
import org.jejuro.miraero.domain.credit.dto.response.CreditScoreEventResponse;
import org.jejuro.miraero.domain.credit.dto.response.CreditScoreResponse;
import org.jejuro.miraero.domain.credit.service.CreditScoreService;
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
@RequestMapping("/api/credit-score")
@Api(tags = "신용점수")
public class CreditScoreController {

    private final CreditScoreService creditScoreService;

    @GetMapping
    @ApiOperation(
            value = "현재 신용점수 조회",
            notes = "로그인 사용자의 현재 신용점수를 조회합니다. 최초 조회 시 665점으로 초기화됩니다."
    )
    public ResponseEntity<ApiResponse<CreditScoreResponse>> getCurrentScore(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        CreditScoreResponse response = creditScoreService.getCurrentScore(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    @ApiOperation(
            value = "신용점수 변동 이력 조회",
            notes = "로그인 사용자의 신용점수 변동 이력을 최신순으로 페이지 조회합니다. page는 1부터 시작합니다."
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "페이지 번호. 1부터 시작", dataType = "int", paramType = "query", example = "1"),
            @ApiImplicitParam(name = "size", value = "페이지당 항목 수", dataType = "int", paramType = "query", example = "20")
    })
    public ResponseEntity<ApiResponse<PageResponse<CreditScoreEventResponse>>> getHistory(
            @ModelAttribute CreditScoreEventSearchCondition condition,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        PageResponse<CreditScoreEventResponse> response =
                creditScoreService.getHistory(user.getUserId(), condition);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
