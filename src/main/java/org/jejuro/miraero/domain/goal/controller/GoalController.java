package org.jejuro.miraero.domain.goal.controller;


import lombok.RequiredArgsConstructor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jejuro.miraero.domain.goal.dto.request.*;
import org.jejuro.miraero.domain.goal.dto.response.*;
import org.jejuro.miraero.domain.goal.service.GoalService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
@Api(tags = "목표")
public class GoalController {

    private final GoalService goalService;

    /**
     * 목표 실현 가능성 조회
     */
    @PostMapping("/possibility")
    @ApiOperation(value = "목표 달성 가능성 확인", notes = "목표 금액·기간·현재 자산을 바탕으로 목표 달성 가능성과 필요한 월 저축액을 계산합니다.")
    public ResponseEntity<ApiResponse<GoalPossibilityResponse>> checkPossibility(
            @Valid @RequestBody GoalPossibilityRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
            ){

        Long userId = user.getUserId();

        GoalPossibilityResponse response = goalService.checkPossibility(request,userId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 목표 생성
     */
    @PostMapping
    @ApiOperation(value = "목표 생성", notes = "로그인 사용자에게 새로운 자산 목표를 생성합니다.")
    public ResponseEntity<ApiResponse<GoalCreateResponse>> createGoal(
            @Valid @RequestBody GoalCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if(request == null){
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        // TODO: 인증 구현 후 userId 가져오기
        Long userId = user.getUserId();

        GoalCreateResponse response =
                goalService.createGoal(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 목표 목록 조회
     */
    @GetMapping
    @ApiOperation(value = "내 목표 목록 조회", notes = "로그인 사용자의 목표 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<GoalListResponse>>> getGoals(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {

        // TODO Security 적용 후 변경
        Long userId = user.getUserId();

        return ResponseEntity.ok(
                ApiResponse.success(
                    goalService.getGoalsByUserId(userId)
                )
        );
    }

    @GetMapping("/collection")
    @ApiOperation(value = "목표 컬렉션 조회", notes = "완료 또는 보관된 목표 컬렉션을 조회합니다.")
    public ResponseEntity<ApiResponse<List<GoalCollectionResponse>>> getCollection(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user.getUserId();
        List<GoalCollectionResponse> responses =
                goalService.getGoalCollections(userId);

        return ResponseEntity.ok(
                ApiResponse.success(responses)
        );
    }

    /**
     * 목표 상세 조회
     */
    @GetMapping("/{goalId}")
    @ApiOperation(value = "목표 상세 조회", notes = "로그인 사용자가 소유한 목표의 상세 정보와 진행 현황을 조회합니다.")
    public ResponseEntity<ApiResponse<GoalDetailResponse>> getGoalDetail(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ){
        Long userId = user.getUserId();
        return ResponseEntity.ok(
                ApiResponse.success(goalService.getGoalDetail(userId,goalId))
        );
    }

    @PatchMapping("/{goalId}")
    @ApiOperation(value = "목표 수정", notes = "로그인 사용자가 소유한 목표 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updateGoal(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable Long goalId,
            @RequestBody GoalUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        //JWT에서 받아오기로
        Long userId = user.getUserId();
        goalService.updateGoal(userId ,goalId, request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{goalId}")
    @ApiOperation(value = "목표 삭제", notes = "로그인 사용자가 소유한 목표를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
    ){
        //JWT 적용 예정
        Long userId = user.getUserId();

        goalService.deleteGoal(userId,goalId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{goalId}/collection")
    @ApiOperation(value = "목표 컬렉션 보관", notes = "목표를 컬렉션으로 보관합니다. 성공 시 data는 null입니다.")
    public ResponseEntity<ApiResponse<Void>> saveCollection(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable Long goalId,
            @AuthenticationPrincipal AuthenticatedUser user
            ){
        Long userId = user.getUserId();

        goalService.saveCollection(userId,goalId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{goalId}/status")
    @ApiOperation(value = "목표 상태 변경", notes = "목표의 진행 상태를 변경합니다. 가능한 상태값은 요청 스키마를 참고하세요.")
    public ResponseEntity<ApiResponse<Void>> updateGoalStatus(
            @ApiParam(value = "목표 ID", example = "1", required = true) @PathVariable Long goalId,
            @Valid @RequestBody GoalStatusUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {

        Long userId = user.getUserId();

        goalService.updateGoalStatus(
                userId,
                goalId,
                request.getStatus()
        );

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }



}
