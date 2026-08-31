package org.jejuro.miraero.domain.transaction.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseCategoryTargetSaveRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryTargetListResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseCategoryTargetService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expense-category-targets")
@Api(tags = "지출 카테고리 목표")
public class ExpenseCategoryTargetController {

    private final ExpenseCategoryTargetService expenseCategoryTargetService;

    @GetMapping
    @ApiOperation(value = "카테고리별 월간 지출 목표 조회", notes = "로그인 사용자가 설정한 카테고리별 월간 지출 목표를 조회합니다.")
    public ResponseEntity<ApiResponse<ExpenseCategoryTargetListResponse>> getTargets(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(expenseCategoryTargetService.getTargets(user.getUserId()))
        );
    }

    @PutMapping
    @ApiOperation(value = "카테고리별 월간 지출 목표 저장", notes = "요청에 포함한 카테고리의 목표 금액을 저장하거나 변경합니다.")
    public ResponseEntity<ApiResponse<ExpenseCategoryTargetListResponse>> saveTargets(
            @Valid @RequestBody ExpenseCategoryTargetSaveRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(expenseCategoryTargetService.saveTargets(user.getUserId(), request))
        );
    }
}
