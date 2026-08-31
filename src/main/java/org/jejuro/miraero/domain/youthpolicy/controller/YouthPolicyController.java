package org.jejuro.miraero.domain.youthpolicy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyDetailResponse;
import org.jejuro.miraero.domain.youthpolicy.dto.response.YouthPolicyListResponse;
import org.jejuro.miraero.domain.youthpolicy.service.YouthPolicyService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.response.PageResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/youth-policies")
@RequiredArgsConstructor
@Api(tags = "청년 정책")
public class YouthPolicyController {

    private final YouthPolicyService youthPolicyService;

    @GetMapping
    @ApiOperation(value = "청년 정책 목록 조회", notes = "키워드·지역·통합 검색어로 청년 정책을 조회합니다. page는 1부터 시작하며, dDay는 신청 마감일까지 남은 일수입니다.")
    public ResponseEntity<ApiResponse<PageResponse<YouthPolicyListResponse>>> getYouthPolicies(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ApiParam(value = "정책 키워드", example = "주거") @RequestParam(required = false) String keyword,
            @ApiParam(value = "시도 코드 또는 지역명. 선택 지역과 전국 정책을 함께 조회", example = "11") @RequestParam(required = false) String region,
            @ApiParam(value = "정책명·내용 통합 검색어", example = "청년 월세") @RequestParam(required = false) String search,
            @ApiParam(value = "페이지 번호. 1부터 시작", example = "1") @RequestParam(defaultValue = "1") int page,
            @ApiParam(value = "페이지당 항목 수", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<YouthPolicyListResponse> response = youthPolicyService.getYouthPolicies(
                user.getUserId(),
                keyword,
                region,
                search,
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recommended")
    @ApiOperation(value = "추천 청년 정책 조회", notes = "로그인 사용자의 나이와 월소득 조건에 맞는 접수 중 정책을 최대 3개 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<YouthPolicyListResponse>>> getRecommendedYouthPolicies(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ApiParam(value = "시도 코드 또는 지역명. 선택 지역과 전국 정책을 함께 조회", example = "11")
            @RequestParam(required = false) String region
    ) {
        PageResponse<YouthPolicyListResponse> response = youthPolicyService
                .getRecommendedYouthPolicies(user.getUserId(), region);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{youthPolicyId}")
    @ApiOperation(value = "청년 정책 상세 조회", notes = "정책 소개, 지원 내용, 신청 기간, 연령·소득 조건, 신청 방법 및 관련 URL을 조회합니다.")
    public ResponseEntity<ApiResponse<YouthPolicyDetailResponse>> getYouthPolicyDetail(
            @ApiParam(value = "청년 정책 ID", example = "1", required = true) @PathVariable Long youthPolicyId
    ) {
        YouthPolicyDetailResponse response = youthPolicyService.getYouthPolicyDetail(youthPolicyId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
