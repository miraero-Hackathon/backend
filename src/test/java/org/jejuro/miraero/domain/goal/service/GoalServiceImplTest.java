package org.jejuro.miraero.domain.goal.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.jejuro.miraero.domain.goal.domain.*;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalUpdateRequest;
import org.jejuro.miraero.domain.goal.dto.response.*;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private GoalAssetService goalAssetService;

    @InjectMocks
    private GoalServiceImpl goalService;

    private GoalPossibilityRequest createRequest(
            Long goalAmount,
            Integer goalMonths,
            Long startAmount
    ) {
        return GoalPossibilityRequest.builder()
                .goalAmount(goalAmount)
                .goalMonths(goalMonths)
                .startAmount(startAmount)
                .build();
    }

    @Test
    @DisplayName("목표 달성이 가능하면 possible은 true를 반환한다")
    void checkPossibility_possible() {
        // given
        GoalPossibilityRequest request =
                createRequest(1_000_000L, 10, 0L);

        Long userId = 1L;
        // when
        GoalPossibilityResponse response =
                goalService.checkPossibility(request,userId);

        assertEquals(100_000L, response.getRequiredMonthly());
        assertEquals(500_000L, response.getAvailableMonthly());
    }

    @Test
    @DisplayName("목표 달성이 불가능하면 possible은 false를 반환한다")
    void checkPossibility_notPossible() {
        // given
        GoalPossibilityRequest request =
                createRequest(10_000_000L, 10, 0L);

        Long userId = 1L;
        // when
        GoalPossibilityResponse response =
                goalService.checkPossibility(request,userId);

        // then
        assertEquals(1_000_000L, response.getRequiredMonthly());
        assertEquals(500_000L, response.getAvailableMonthly());
    }

    @Test
    @DisplayName("시작 금액을 고려하여 필요 월별 금액을 계산한다")
    void checkPossibility_withStartAmount() {
        // given
        GoalPossibilityRequest request =
                createRequest(1_000_000L, 10, 200_000L);
        Long userId = 1L;
        // when
        GoalPossibilityResponse response =
                goalService.checkPossibility(request,userId);

        // then
        assertEquals(80_000L, response.getRequiredMonthly());

    }

    @Test
    @DisplayName("목표 생성 성공 시 생성된 goalId를 반환한다.")
    void createGoal_success() {
        // given
        Long userId = 1L;

        GoalCreateRequest request =
                GoalCreateRequest.builder()
                        .goalType(GoalType.WEDDING)
                        .goalName("결혼 자금")
                        .goalAmount(20_000_000L)
                        .goalMonths(24)
                        .startAmount(1_000_000L)
                        .assets(List.of(
                                GoalAssetRequest.builder()
                                        .assetType(AssetType.ACCOUNT)
                                        .assetId(1L)
                                        .build(),
                                GoalAssetRequest.builder()
                                        .assetType(AssetType.MONEY_BOX)
                                        .assetId(2L)
                                        .build()
                        ))
                        .build();


        // goalMapper.save() 실행 시 goalId 주입
        doAnswer(invocation -> {

            Goal goal = invocation.getArgument(0);
            goal.setGoalId(1L);

            return null;

        }).when(goalMapper).save(any(Goal.class));


        // when
        GoalCreateResponse response =
                goalService.createGoal(userId, request);


        // then
        assertEquals(1L, response.getGoalId());


        verify(goalMapper)
                .save(any(Goal.class));


        verify(goalAssetService)
                .saveGoalAssets(
                        eq(1L),
                        eq(1L),
                        eq(request.getAssets())
                );
    }

    @Test
    @DisplayName("사용자의 목표 목록을 조회하여 반환한다.")
    void getGoalsByUserId_success() {
        // given
        Long userId = 1L;

        Goal goal1 = Goal.builder()
                .goalId(1L)
                .goalName("결혼 자금")
                .goalType(GoalType.WEDDING)
                .goalAmount(2_000_000L)
                .startAmount(500_000L)
                .goalStatus(GoalStatus.ACTIVE)
                .build();

        Goal goal2 = Goal.builder()
                .goalId(2L)
                .goalName("독립 자금")
                .goalType(GoalType.INDEPENDENCE)
                .goalAmount(10_000_000L)
                .startAmount(200_000L)
                .goalStatus(GoalStatus.ACTIVE)
                .build();

        when(goalMapper.findGoalsByUserId(userId)).thenReturn(List.of(goal1,goal2));


        // 연결 자산에서 계산된 현재 금액 Mock
        when(goalAssetService.calculateCurrentAmount(1L,1L)).thenReturn(500_000L);
        when(goalAssetService.calculateCurrentAmount(1L,2L)).thenReturn(200_000L);

        // when
        List<GoalListResponse> result =
                goalService.getGoalsByUserId(userId);

        // then
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getGoalId());
        assertEquals("결혼 자금", result.get(0).getGoalName());
        assertEquals(25, result.get(0).getProgressRate());

        assertEquals(2L, result.get(1).getGoalId());
        assertEquals("독립 자금", result.get(1).getGoalName());
        assertEquals(2, result.get(1).getProgressRate());

        verify(goalMapper).findGoalsByUserId(userId);
        verify(goalAssetService)
                .calculateCurrentAmount(1L,1L);

        verify(goalAssetService)
                .calculateCurrentAmount(1L,2L);
    }

    @Test
    @DisplayName("목표 상세 조회 성공")
    void getGoalDetail_success() {

        // given
        Long userId = 1L;
        Long goalId = 1L;

        Goal goal = Goal.builder()
                .goalId(goalId)
                .goalName("결혼 자금")
                .goalType(GoalType.WEDDING)
                .goalAmount(20_000_000L)
                .startAmount(1_000_000L)
                .startDate(LocalDate.now())
                .goalDate(LocalDate.now().plusMonths(24))
                .goalStatus(GoalStatus.ACTIVE)
                .build();


        when(goalMapper.findByIdAndUserId(userId, goalId))
                .thenReturn(goal);

        when(goalAssetService.calculateCurrentAmount(1L,goalId))
                .thenReturn(5_000_000L);


        // when
        GoalDetailResponse response =
                goalService.getGoalDetail(userId, goalId);


        // then
        assertEquals("결혼 자금", response.getGoalName());
        assertEquals(5_000_000L, response.getCurrentAmount());
        assertEquals(25, response.getProgressRate());

        verify(goalMapper)
                .findByIdAndUserId(userId, goalId);

    }

    @Test
    @DisplayName("존재하지 않는 목표 조회 시 예외 발생")
    void getGoalDetail_notFound() {

        // given
        Long userId = 1L;
        Long goalId = 999L;

        when(goalMapper.findByIdAndUserId(userId, goalId))
                .thenReturn(null);


        // then
        assertThrows(
                BusinessException.class,
                () -> goalService.getGoalDetail(userId, goalId)
        );
    }

    @Test
    @DisplayName("목표 수정 성공")
    void updateGoal_success() {
        // given
        Long userId = 1L;
        Long goalId = 1L;

        Goal goal = Goal.builder()
                .goalId(goalId)
                .userId(userId)
                .goalName("결혼 자금")
                .goalAmount(20000000L)
                .build();

        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .goalName("수정된 목표")
                .goalAmount(30000000L)
                .goalMonths(24)
                .build();

        when(goalMapper.findByIdAndUserId(userId, goalId))
                .thenReturn(goal);

        // when
        goalService.updateGoal(userId, goalId, request);

        // then
        verify(goalMapper).update(any(Goal.class));
    }

    @Test
    @DisplayName("목표 삭제 성공")
    void deleteGoal_success() {
        // given
        Long userId = 1L;
        Long goalId = 10L;

        Goal goal = Goal.builder()
                .goalId(goalId)
                .userId(userId)
                .goalName("여행")
                .goalAmount(1000000L)
                .build();

        when(goalMapper.findByIdAndUserId(userId, goalId))
                .thenReturn(goal);

        // when
        goalService.deleteGoal(userId, goalId);

        // then
        verify(goalMapper, times(1))
                .delete(goalId);
    }

    @Test
    @DisplayName("완료된 목표는 컬렉션 저장 성공")
    void saveCollection_success() {

        Long userId = 1L;
        Long goalId = 10L;

        Goal goal = Goal.builder()
                .goalId(goalId)
                .userId(userId)
                .goalStatus(GoalStatus.COMPLETED)
                .build();

        when(goalMapper.findByIdAndUserId(userId, goalId))
                .thenReturn(goal);


        goalService.saveCollection(userId, goalId);


        verify(goalMapper)
                .updateCollection(userId, goalId);
    }

    @Test
    @DisplayName("목표 달성률이 100%이면 목표 상태가 COMPLETED로 변경된다")
    void getGoalDetail_completeGoal_success() {

        // given
        Long userId = 1L;
        Long goalId = 10L;

        Goal goal = Goal.builder()
                .goalId(goalId)
                .userId(userId)
                .goalName("여행")
                .goalAmount(1_000_000L)
                .startAmount(0L)
                .goalStatus(GoalStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .goalDate(LocalDate.of(2026, 12, 31))
                .build();

        given(goalMapper.findByIdAndUserId(userId, goalId))
                .willReturn(goal);

        // 현재 금액 = 목표 금액 → 달성률 100%
        given(goalAssetService.calculateCurrentAmount(1L,goalId))
                .willReturn(1_000_000L);


        // when
        GoalDetailResponse response =
                goalService.getGoalDetail(userId, goalId);


        // then
        verify(goalMapper)
                .updateCompleteStatus(goalId);

        assertEquals(
                GoalStatus.COMPLETED,
                response.getStatus()
        );
    }
    @Test
    @DisplayName("컬렉션 목표 조회 성공")
    void getGoalCollections_success() {

        // given
        Long userId = 1L;

        Goal goal = Goal.builder()
                .goalId(10L)
                .userId(userId)
                .goalName("제주도 여행")
                .goalAmount(3000000L)
                .goalType(GoalType.EMERGENCY)
                .startDate(LocalDate.of(2026, 1, 1))
                .goalDate(LocalDate.of(2026, 12, 31))
                .build();

        given(goalMapper.findGoalCollectionsByUserId(userId))
                .willReturn(List.of(goal));


        // when
        List<GoalCollectionResponse> response =
                goalService.getGoalCollections(userId);


        // then
        assertEquals(1, response.size());

        GoalCollectionResponse result = response.get(0);

        assertEquals(10L, result.getGoalId());
        assertEquals("제주도 여행", result.getGoalName());
        assertEquals(3000000L, result.getGoalAmount());
        assertEquals(GoalType.EMERGENCY, result.getGoalType());

        verify(goalMapper)
                .findGoalCollectionsByUserId(userId);
    }

    @Test
    @DisplayName("목표 일시정지 성공")
    void updateGoalStatus_pause_success() {

        // given
        Long userId = 1L;
        Long goalId = 1L;

        Goal goal = Goal.builder()
                .goalId(goalId)
                .userId(userId)
                .goalStatus(GoalStatus.ACTIVE)
                .build();


        given(goalMapper.findByIdAndUserId(
                userId,
                goalId
        ))
                .willReturn(goal);


        // when
        goalService.updateGoalStatus(
                userId,
                goalId,
                GoalStatus.PAUSED
        );


        // then
        verify(goalMapper)
                .updateStatus(
                        goalId,
                        GoalStatus.PAUSED
                );
    }

    @Test
    @DisplayName("완료된 목표는 상태 변경 불가")
    void updateGoalStatus_completed_fail() {

        Long userId = 1L;
        Long goalId = 1L;


        Goal goal = Goal.builder()
                .goalId(goalId)
                .goalStatus(GoalStatus.COMPLETED)
                .build();


        given(goalMapper.findByIdAndUserId(
                userId,
                goalId
        ))
                .willReturn(goal);



        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> goalService.updateGoalStatus(
                                userId,
                                goalId,
                                GoalStatus.ACTIVE
                        )
                );


        assertEquals(
                GoalErrorCode.GOAL_COMPLETED,
                exception.getErrorCode()
        );


        verify(goalMapper, never())
                .updateStatus(any(), any());
    }

}