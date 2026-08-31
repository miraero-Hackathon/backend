package org.jejuro.miraero.domain.goal.controller;

import lombok.RequiredArgsConstructor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalPullFundsResponse;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetListResponse;
import org.jejuro.miraero.domain.goal.service.GoalAssetService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
@Api(tags = "목표 - 연결 자산")
public class GoalAssetController {

    private final GoalAssetService goalAssetService;

    //목표와 자산 연결
    @PostMapping("/{goalId}/assets")
    @ApiOperation(value = "목표에 자산 연결", notes = "목표 달성 금액에 반영할 계좌 또는 머니박스를 연결합니다. 요청 목록 전체를 저장합니다.")
    public ResponseEntity<ApiResponse<Void>> addGoalAssets(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable Long goalId,
            @Valid @RequestBody List<GoalAssetRequest> request,
            @AuthenticationPrincipal AuthenticatedUser user
    ){
        Long userId = user.getUserId();

        goalAssetService.saveGoalAssets(userId,goalId,request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }


    /**
     * 목표 연결 자산 조회
     */
    @GetMapping("/{goalId}/assets")
    @ApiOperation(value = "목표 연결 자산 조회", notes = "목표에 연결된 자산 목록과 금액 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<GoalAssetListResponse>> getGoalAssets(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        goalAssetService.getGoalAssets(user.getUserId(), goalId)
                )
        );
    }

    @PostMapping("/{goalId}/pull-funds")
    @ApiOperation(
            value = "끌어쓰기",
            notes = "여유자금 부족 등으로 목표가 밀렸을 때, 목표에 연결되지 않은 다른 입출금 계좌의 돈을 목표 자산에 채웁니다."
    )
    public ResponseEntity<ApiResponse<GoalPullFundsResponse>> pullFunds(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable Long goalId,
            @Valid @RequestBody GoalPullFundsRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                goalAssetService.pullFunds(user.getUserId(), goalId, request)));
    }

    @DeleteMapping("/{goalId}/assets/{assetType}/{assetId}")
    @ApiOperation(value = "목표 연결 자산 해제", notes = "목표에서 특정 계좌 또는 머니박스 연결을 해제합니다. 성공 시 data는 null입니다.")
    public ResponseEntity<ApiResponse<Void>> deleteGoalAsset(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable Long goalId,
            @ApiParam(value = "자산 유형", example = "ACCOUNT", required = true) @PathVariable AssetType assetType,
            @ApiParam(value = "자산 ID", example = "1", required = true) @PathVariable Long assetId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {

        goalAssetService.deleteGoalAsset(
                user.getUserId(),
                goalId,
                assetType,
                assetId
        );

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }


}
