package org.jejuro.miraero.domain.goal.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.availablemoney.dto.response.MonthlyAvailableMoneyResponse;
import org.jejuro.miraero.domain.availablemoney.service.AvailableMoneyService;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.service.CreditScoreService;
import org.jejuro.miraero.domain.goal.calculator.GoalPaceCalculator;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalPossibility;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalCreateRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalUpdateRequest;
import org.jejuro.miraero.domain.goal.dto.response.*;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.milestone.service.MilestoneService;
import org.jejuro.miraero.domain.transaction.service.ExpenseCategoryTargetService;
import org.jejuro.miraero.domain.user.service.UserService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService{


    private final GoalMapper goalMapper;
    private final GoalAssetService goalAssetService;
    private final MilestoneService milestoneService;
    private final ExpenseCategoryTargetService expenseCategoryTargetService;
    private final AvailableMoneyService availableMoneyService;
    private final UserService userService;
    private final GoalPaceCalculator goalPaceCalculator;
    private final CreditScoreService creditScoreService;


    /**
     * 목표 실현 가능성 조회
     *
     * 목표 금액, 시작 금액, 목표 기간을 기준으로
     * 필요한 월 저축 금액을 계산하고 사용 가능한 금액과 비교하여
     * 목표 달성 가능 여부를 반환한다.
     *
     * @param request 목표 실현 가능성 조회 요청 정보
     * @return 목표 실현 가능성 정보
     */
    @Override
    @Transactional(readOnly = true)
    public GoalPossibilityResponse checkPossibility(
            GoalPossibilityRequest request,
            Long userId
            )
    {
        if(request == null){
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        MonthlyAvailableMoneyResponse availableMoney =
                availableMoneyService.getMonthlyAvailableMoney(userId, null);

        // 여유자금의 70% 를 저축 가능금액으로 설정
        long monthlySavingAmount =
                availableMoney.getMonthlyAvailableMoney()*70/100;

        long requiredMonthly = calculateRequiredMonthlyDisplay(
                request.getGoalAmount(),
                request.getStartAmount(),
                request.getGoalMonths()
        );

        GoalPossibility possibility;
        if(monthlySavingAmount <= 0) {
            possibility = GoalPossibility.DIFFICULT;
        }else{
            double possible = (double) requiredMonthly / monthlySavingAmount;
            if (possible <= 1) possibility = GoalPossibility.REALISTIC;
            else if (possible <= 1.2) possibility = GoalPossibility.TIGHT;
            else possibility = GoalPossibility.DIFFICULT;
        }


        return GoalPossibilityResponse.builder()
                .availableMonthly(monthlySavingAmount)
                .requiredMonthly(requiredMonthly)
                .possible(possibility)
                .build();
    }


    private Long calculateRequiredMonthlyDisplay(
            Long goalAmount,
            Long startAmount,
            Integer goalMonths
    ){
        validateGoalInput(goalAmount,startAmount,goalMonths);

        if (startAmount >= goalAmount) {
            return 0L;
        }

        long remainingAmount= goalAmount - startAmount;

        long monthlyAmount = remainingAmount / goalMonths;

        return ((monthlyAmount+9999)/10000)*10000;
    }


    /**
     * 목표 생성
     *
     * 사용자가 입력한 목표 정보를 기반으로 목표를 생성하고,
     * 생성된 목표와 연결할 자산 정보를 저장한다.
     *
     * @param userId 사용자 ID
     * @param request 목표 생성 요청 정보
     * @return 생성된 목표 ID
     */
    @Override
    @Transactional
    public GoalCreateResponse createGoal(Long userId, GoalCreateRequest request) {

        if(request == null){
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        validateGoalInput(
                request.getGoalAmount(),
                request.getStartAmount(),
                request.getGoalMonths()
        );

        //1. 목표 생성
        LocalDate startDate = LocalDate.now();
        LocalDate goalDate = startDate
                .plusMonths(request.getGoalMonths())
                .with(TemporalAdjusters.lastDayOfMonth());

        Goal goal = Goal.builder()
                .userId(userId)
                .goalType(request.getGoalType())
                .goalName(request.getGoalName())
                .goalAmount(request.getGoalAmount())
                .startAmount(request.getStartAmount())
                .goalDate(goalDate)
                .startDate(startDate)
                .build();

        goalMapper.save(goal);

        //2. 목표 마일스톤 4개 생성
        milestoneService.createMilestones(
                goal.getGoalId(),
                goal.getGoalAmount()
        );

        //3. 목표 자산 연결
        goalAssetService.saveGoalAssets(
                userId,
                goal.getGoalId(),
                request.getAssets()
        );

        //4. 시작 금액을 연결된 저금통 잔액으로 반영
        goalAssetService.applyStartAmount(
                goal.getGoalId(),
                request.getStartAmount()
        );

        //5. 신용점수 코어 초기화 (없으면 665점 생성) — 대출 시뮬레이션(2단계)이
        //   나중에 이 목표를 미러링해서 판정을 시작할 텐데, 그 전에 신용점수가
        //   이미 존재하는 걸 보장해두기 위함.
        creditScoreService.initializeCreditScore(userId);




        return GoalCreateResponse.builder()
                .goalId(goal.getGoalId())
                .build();
    }

    /**
     * 사용자의 목표 목록을 조회
     *
     * 사용자가 생성한 목표 목록을 조회하고,
     * 각 목표의 현재 달성 금액 및 진행률을 계산하여 반환한다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 목표 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<GoalListResponse> getGoalsByUserId(Long userId) {

        List<Goal> goals =
                goalMapper.findGoalsByUserId(userId);

        if (goals == null || goals.isEmpty()) {
            return List.of();
        }

        // 목표 ID 일괄 추출
        List<Long> goalIds = goals.stream()
                .map(Goal::getGoalId)
                .toList();

        // 모든 목표의 현재 금액을 한 번에 계산
        Map<Long, Long> currentAmountMap =
                goalAssetService.calculateCurrentAmounts(
                        userId,
                        goalIds
                );

        return goals.stream()
                .map(goal -> {

                    Long currentAmount =
                            currentAmountMap.getOrDefault(
                                    goal.getGoalId(),
                                    0L
                            );

                    Integer progressRate =
                            calculateProgressRate(
                                    currentAmount,
                                    goal.getGoalAmount()
                            );

                    return GoalListResponse.from(
                            goal,
                            progressRate
                    );
                })
                .toList();
    }

    /**
     * 목표 진행률 계산 소수점 버림 처리
     */
    private Integer calculateProgressRate(Long currentAmount, Long goalAmount) {
        if (goalAmount == null || goalAmount == 0) {
            return 0;
        }

        if(currentAmount == null) {
            return 0;
        }

        return Math.min(100,
                (int) (currentAmount * 100.0 / goalAmount)
        );
    }

    /**
     * 목표 상세 조회
     *
     * 특정 목표의 기본 정보와 연결된 자산 정보를 기반으로
     * 현재 진행률, 기간 정보, 목표 달성 페이스를 계산하여 반환한다.
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @return 목표 상세 정보
     */
    @Override
    @Transactional
    public GoalDetailResponse getGoalDetail(Long userId, Long goalId) {

        // 목표 기본 정보 조회
        Goal goal = goalMapper.findByIdAndUserId(userId,goalId);

        if(goal == null){
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        // 연결된 자산들의 현재 금액 계산
        Long currentAmount = Optional.ofNullable(
                goalAssetService.calculateCurrentAmount(userId, goalId)
        ).orElse(0L);

        // 마일스톤
        milestoneService.updatedMilestoneAchievement(
                goalId,
                currentAmount
        );

        // 진행률 계산
        int progressRate = calculateProgressRate(
                currentAmount,
                goal.getGoalAmount()
        );

        // 목표 완료 처리
        if (progressRate == 100
                && goal.getGoalStatus() != GoalStatus.COMPLETED) {

            goalMapper.updateCompleteStatus(goal.getGoalId());
            goal.changeStatus(GoalStatus.COMPLETED);

            // 대출 시뮬레이션: 원금 완납 가점. 이 if문 자체가 "상태가 COMPLETED가
            // 아닐 때만" 조건이라, DB에 이미 COMPLETED로 저장된 뒤 재조회하면
            // 이 블록에 다시 안 들어와서 자연스럽게 "딱 한 번만" 발생함
            // (goalMapper.updateCompleteStatus가 먼저 실행돼서 다음 조회부턴 이미 COMPLETED이므로).
            creditScoreService.applyEvent(
                    userId,
                    100,
                    CreditScoreReasonCode.LOAN_FULLY_REPAID,
                    "'" + goal.getGoalName() + "' 원금 완납",
                    LocalDateTime.now()
            );
        }

        // 기간 정보 생성
        GoalPeriodResponse period = GoalPeriodResponse.builder()
                .goalMonths(
                        (int) ChronoUnit.MONTHS.between(
                                YearMonth.from(goal.getStartDate()),
                                YearMonth.from(goal.getGoalDate())
                        )
                )
                .startDate(YearMonth.from(goal.getStartDate()))
                .endDate(YearMonth.from(goal.getGoalDate()))
                .remainMonths(
                        Math.max(0,(int) ChronoUnit.MONTHS.between(
                                YearMonth.now(),
                                YearMonth.from(goal.getGoalDate())
                        ))
                )
                .build();

        // 페이스 계산
        GoalPaceResponse pace = goalPaceCalculator.calculate(
                goal,
                currentAmount
        );

        return GoalDetailResponse.builder()
                .goalId(goal.getGoalId())
                .goalName(goal.getGoalName())
                .goalType(goal.getGoalType())
                .goalAmount(goal.getGoalAmount())
                .currentAmount(currentAmount)
                .startAmount(goal.getStartAmount())
                .progressRate(progressRate)
                .status(goal.getGoalStatus())
                .period(period)
                .pace(pace)
                .build();
    }

    private void validateGoalInput(
            Long goalAmount,
            Long startAmount,
            Integer goalMonths
    ) {

        if (goalAmount == null || goalAmount <= 0) {
            throw new BusinessException(GoalErrorCode.INVALID_GOAL_AMOUNT);
        }

        if (startAmount == null || startAmount < 0) {
            throw new BusinessException(GoalErrorCode.INVALID_START_AMOUNT);
        }

        if (goalMonths == null || goalMonths <= 0) {
            throw new BusinessException(GoalErrorCode.INVALID_GOAL_MONTHS);
        }

        if ( startAmount > goalAmount){
            throw new BusinessException(GoalErrorCode.START_AMOUNT_EXCEEDS_GOAL_AMOUNT);
        }
    }

    /**
     * 목표 수정
     *
     * 사용자가 생성한 목표 정보를 수정한다.
     * 요청 값 중 전달된 값만 변경하며,
     * 전달되지 않은 값은 기존 정보를 유지한다.
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @param request 목표 수정 요청 정보
     */
    @Override
    @Transactional
    public void updateGoal(Long userId, Long goalId, GoalUpdateRequest request) {

        Goal goal = goalMapper.findByIdAndUserId(userId, goalId);

        if (goal == null) {
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        validateUpdateRequest(goal,request);

        Long previousGoalAmount = goal.getGoalAmount();

        LocalDate goalDate = null;

        if (request.getGoalMonths() != null) {
            goalDate = LocalDate.now()
                    .plusMonths(request.getGoalMonths())
                    .with(TemporalAdjusters.lastDayOfMonth());
        }

        goal.update(
                request.getGoalName(),
                request.getGoalAmount(),
                goalDate
        );

        goalMapper.update(goal);

        if (request.getGoalAmount() != null
                && !previousGoalAmount.equals(request.getGoalAmount())) {

            milestoneService.recreateMilestones(
                    goalId,
                    request.getGoalAmount()
            );
        }
    }

    private void validateUpdateRequest(Goal goal, GoalUpdateRequest request) {

        if (request == null) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }

        if (request.getGoalAmount() != null
                && request.getGoalAmount() <= 0) {
            throw new BusinessException(
                    GoalErrorCode.INVALID_GOAL_AMOUNT
            );
        }


        if (request.getGoalName() != null
                && request.getGoalName().isBlank()) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (request.getGoalMonths() != null
                && request.getGoalMonths() <= 0) {
            throw new BusinessException(
                    GoalErrorCode.INVALID_GOAL_MONTHS
            );
        }

        if (request.getGoalAmount() != null
                && goal.getStartAmount() > request.getGoalAmount()) {
            throw new BusinessException(
                    GoalErrorCode.START_AMOUNT_EXCEEDS_GOAL_AMOUNT
            );
        }
    }

    /**
     * 목표 삭제
     *
     * 사용자가 생성한 목표를 삭제한다.
     * 목표와 연결된 자산 정보는 FK CASCADE 설정에 의해 함께 삭제된다.
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     */
    @Override
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = goalMapper.findByIdAndUserId(userId,goalId);

        if (goal == null) {
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        // 저금통은 goal_asset과 FK가 없어 목표만 지우면 잔액이 묶인 채 남는다
        goalAssetService.releaseGoalAssets(goalId);

        // 목표 삭제
        goalMapper.delete(goalId);
    }

    /**
     * 목표 컬렉션 저장
     *
     * 사용자가 완료한 목표를 컬렉션에 저장한다.
     * 본인의 목표인지 검증하며, 목표 상태가 COMPLETED인 경우에만 저장 가능하다.
     *
     * @param userId 사용자ID
     * @param goalId 저장할 목표 ID
     */
    @Override
    @Transactional
    public void saveCollection(Long userId, Long goalId) {

        Goal goal = goalMapper.findByIdAndUserId(userId,goalId);

        if( goal == null){
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        if(goal.getGoalStatus() != GoalStatus.COMPLETED){
            throw new BusinessException(GoalErrorCode.GOAL_NOT_COMPLETED);
        }

        goalMapper.updateCollection(userId,goalId);
    }


    /**
     * 목표 컬렉션 조회
     *
     * 사용자가 컬렉션에 저장한 완료 목표 목록을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 컬렉션 목표 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<GoalCollectionResponse> getGoalCollections(Long userId) {

        List<Goal> goals = goalMapper.findGoalCollectionsByUserId(userId);

        return goals.stream()
                .map(GoalCollectionResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void updateGoalStatus(Long userId, Long goalId, GoalStatus status) {
        Goal goal = goalMapper.findByIdAndUserId(userId,goalId);

        if(goal == null){
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        if (status == null) {
            throw new BusinessException(
                    GoalErrorCode.INVALID_STATUS_CHANGE
            );
        }

        if(goal.getGoalStatus() == GoalStatus.COMPLETED){
            throw new BusinessException(GoalErrorCode.GOAL_COMPLETED);
        }

        if(status == GoalStatus.COMPLETED){
            throw new BusinessException(
                    GoalErrorCode.INVALID_STATUS_CHANGE
            );
        }

        goalMapper.updateStatus(goalId,status);
    }
}
