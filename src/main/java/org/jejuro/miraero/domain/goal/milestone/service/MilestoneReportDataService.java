package org.jejuro.miraero.domain.goal.milestone.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.domain.SavingHistorySummary;
import org.jejuro.miraero.domain.autotransfer.mapper.SavingHistoryMapper;
import org.jejuro.miraero.domain.goal.calculator.GoalPaceCalculator;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.dto.response.GoalPaceResponse;
import org.jejuro.miraero.domain.goal.service.GoalAssetService;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.dto.request.MilestoneReportAiRequest;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.transaction.domain.ExpenseCategory;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategorySummaryResponse;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MilestoneReportDataService {

    /*
     * 마일스톤 단계는 현재 기획상 고정
     */
    private static final List<Integer> MILESTONE_PERCENTAGES =
            List.of(25, 50, 75, 100);

    private final MilestoneMapper milestoneMapper;
    private final TransactionMapper transactionMapper;
    private final SavingHistoryMapper savingHistoryMapper;
    private final GoalAssetService goalAssetService;
    private final GoalPaceCalculator goalPaceCalculator;

    /**
     * AI 요청에 필요한 모든 데이터를 구성한다.
     */
    public MilestoneReportAiRequest buildAiRequest(
            Goal goal,
            Milestone milestone
    ) {

        LocalDate expectedTargetDate =
                calculateTargetDate(
                        goal,
                        milestone.getMilestonePercentage()
                );

        LocalDate reportEndDate =
                resolveReportEndDate(
                        goal,
                        milestone
                );

        Milestone previousMilestone =
                findPreviousDistinctMilestone(
                        goal,
                        milestone
                );

        LocalDate periodStartDate =
                resolveCurrentPeriodStart(
                        goal,
                        previousMilestone
                );

        MilestoneReportAiRequest.ExpenseSummary expenseSummary =
                summarizeExpenses(
                        goal.getUserId(),
                        periodStartDate,
                        reportEndDate,
                        previousMilestone,
                        goal
                );

        return MilestoneReportAiRequest.builder()
                .goal(
                        MilestoneReportAiRequest.GoalInfo.builder()
                                .goalName(
                                        goal.getGoalName()
                                )
                                .goalType(
                                        goal.getGoalType() == null
                                                ? null
                                                : goal.getGoalType().name()
                                )
                                .goalAmount(
                                        goal.getGoalAmount()
                                )
                                .startAmount(
                                        goal.getStartAmount()
                                )
                                .startDate(
                                        goal.getStartDate()
                                )
                                .goalDate(
                                        goal.getGoalDate()
                                )
                                .build()
                )
                .milestone(
                        MilestoneReportAiRequest.MilestoneInfo.builder()
                                .percentage(
                                        milestone.getMilestonePercentage()
                                )
                                .milestoneAmount(
                                        milestone.getMilestoneAmount()
                                )
                                .targetDate(
                                        expectedTargetDate
                                )
                                .achievedAt(
                                        milestone.getAchievedAt() == null
                                                ? null
                                                : milestone
                                                .getAchievedAt()
                                                .toLocalDate()
                                )
                                .build()
                )
                .expenseSummary(
                        expenseSummary
                )
                .pace(
                        buildPaceInfo(goal)
                )
                .savingSummary(
                        buildSavingSummary(
                                goal.getUserId(),
                                periodStartDate,
                                reportEndDate
                        )
                )
                .build();
    }

    /**
     * 목표 진행 속도를 계산한다.
     *
     * 계산에 실패해도 리포트 자체는 생성되어야 하므로 null을 반환한다.
     * 프롬프트가 pace 없으면 진행 속도를 언급하지 않도록 지시받는다.
     */
    private MilestoneReportAiRequest.PaceInfo buildPaceInfo(
            Goal goal
    ) {

        if (goal.getStartDate() == null
                || goal.getGoalDate() == null) {

            return null;
        }

        Long currentAmount =
                goalAssetService.calculateCurrentAmount(
                        goal.getUserId(),
                        goal.getGoalId()
                );

        if (currentAmount == null) {
            currentAmount = 0L;
        }

        GoalPaceResponse pace =
                goalPaceCalculator.calculate(
                        goal,
                        currentAmount
                );

        return MilestoneReportAiRequest.PaceInfo.builder()
                .currentAmount(currentAmount)
                .expectedAmount(pace.getExpectedAmount())
                .differenceAmount(pace.getDifferenceAmount())
                .paceStatus(
                        pace.getPaceStatus() == null
                                ? null
                                : pace.getPaceStatus().name()
                )
                .build();
    }

    /**
     * 지출과 같은 구간의 적립 이력을 집계한다.
     *
     * 구간을 맞춰야 "소비는 늘었지만 적립은 지켰다" 같은 대비가 가능하다.
     */
    private MilestoneReportAiRequest.SavingSummary buildSavingSummary(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (userId == null
                || startDate == null
                || endDate == null
                || endDate.isBefore(startDate)) {

            return emptySavingSummary();
        }

        SavingHistorySummary summary =
                savingHistoryMapper.findSummary(
                        userId,
                        startDate,
                        endDate
                );

        if (summary == null) {
            return emptySavingSummary();
        }

        return MilestoneReportAiRequest.SavingSummary.builder()
                .attemptCount(zeroIfNull(summary.getAttemptCount()))
                .successCount(zeroIfNull(summary.getSuccessCount()))
                .partialCount(zeroIfNull(summary.getPartialCount()))
                .failedCount(zeroIfNull(summary.getFailedCount()))
                .totalSavedAmount(zeroIfNull(summary.getTotalSavedAmount()))
                .maxStreak(zeroIfNull(summary.getMaxStreak()))
                .build();
    }

    private MilestoneReportAiRequest.SavingSummary emptySavingSummary() {

        return MilestoneReportAiRequest.SavingSummary.builder()
                .attemptCount(0L)
                .successCount(0L)
                .partialCount(0L)
                .failedCount(0L)
                .totalSavedAmount(0L)
                .maxStreak(0L)
                .build();
    }

    private Long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * 현재 마일스톤 이전의
     * 가장 최근 다른 날짜의 마일스톤을 조회한다.
     *
     * 같은 날짜에 달성된 마일스톤은
     * 새로운 지출 분석 구간의 경계로 사용하지 않는다.
     */
    private Milestone findPreviousDistinctMilestone(
            Goal goal,
            Milestone currentMilestone
    ) {

        if (goal == null
                || goal.getGoalId() == null
                || currentMilestone == null
                || currentMilestone.getMilestonePercentage() == null
                || currentMilestone.getAchievedAt() == null) {

            return null;
        }

        LocalDate currentAchievedDate =
                currentMilestone
                        .getAchievedAt()
                        .toLocalDate();

        Integer currentPercentage =
                currentMilestone
                        .getMilestonePercentage();

        Milestone previousMilestone = null;

        for (Integer percentage :
                MILESTONE_PERCENTAGES) {

            if (percentage >= currentPercentage) {
                continue;
            }

            Milestone candidate =
                    milestoneMapper.findByGoalIdAndPercentage(
                            goal.getGoalId(),
                            percentage
                    );

            if (candidate == null
                    || !candidate.isAchieved()
                    || candidate.getAchievedAt() == null) {
                continue;
            }

            LocalDate candidateDate =
                    candidate
                            .getAchievedAt()
                            .toLocalDate();

            if (!candidateDate.isBefore(
                    currentAchievedDate
            )) {
                continue;
            }

            if (previousMilestone == null
                    || candidateDate.isAfter(
                    previousMilestone
                            .getAchievedAt()
                            .toLocalDate()
            )) {

                previousMilestone = candidate;
            }
        }

        return previousMilestone;
    }

    /**
     * 현재 지출 분석 구간의 시작일
     */
    private LocalDate resolveCurrentPeriodStart(
            Goal goal,
            Milestone previousMilestone
    ) {

        if (previousMilestone == null
                || previousMilestone.getAchievedAt() == null) {

            return goal.getStartDate();
        }

        return previousMilestone
                .getAchievedAt()
                .toLocalDate()
                .plusDays(1);
    }

    /**
     * 현재 기간의 지출 데이터를 분석한다.
     */
    private MilestoneReportAiRequest.ExpenseSummary summarizeExpenses(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Milestone previousMilestone,
            Goal goal
    ) {

        if (startDate == null || endDate == null) {
            return buildEmptyExpenseSummary();
        }

        if (endDate.isBefore(startDate)) {
            return buildEmptyExpenseSummary(
                    startDate,
                    endDate
            );
        }

        List<ExpenseCategorySummaryResponse> currentSummaries =
                transactionMapper.findVariableExpenseSummary(
                        userId,
                        startDate,
                        endDate
                );

        PreviousExpensePeriod previousPeriod =
                findPreviousExpensePeriod(
                        userId,
                        previousMilestone,
                        goal
                );

        Map<ExpenseCategory, Long> currentAmounts =
                aggregateByCategory(
                        currentSummaries
                );

        Map<ExpenseCategory, Long> previousAmounts =
                aggregateByCategory(
                        previousPeriod.summaries()
                );

        long totalExpense =
                currentAmounts.values()
                        .stream()
                        .mapToLong(Long::longValue)
                        .sum();

        long currentDays =
                calculateInclusiveDays(
                        startDate,
                        endDate
                );

        long dailyAverageExpense =
                calculateDailyAverage(
                        totalExpense,
                        currentDays
                );

        List<MilestoneReportAiRequest.CategoryExpense> categories =
                buildCategories(
                        currentAmounts,
                        previousAmounts,
                        totalExpense,
                        currentDays,
                        previousPeriod.days()
                );

        return MilestoneReportAiRequest.ExpenseSummary.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalExpense(
                        Math.max(0L, totalExpense)
                )
                .dailyAverageExpense(
                        dailyAverageExpense
                )
                .categories(categories)
                .build();
    }

    /**
     * 이전 비교 기간의 지출 데이터를 조회한다.
     */
    private PreviousExpensePeriod findPreviousExpensePeriod(
            Long userId,
            Milestone previousMilestone,
            Goal goal
    ) {

        if (previousMilestone == null
                || previousMilestone.getAchievedAt() == null
                || goal == null
                || goal.getStartDate() == null) {

            return PreviousExpensePeriod.empty();
        }

        LocalDate previousEndDate =
                previousMilestone
                        .getAchievedAt()
                        .toLocalDate();

        Milestone previousPreviousMilestone =
                findPreviousDistinctMilestone(
                        goal,
                        previousMilestone
                );

        LocalDate previousStartDate =
                goal.getStartDate();

        if (previousPreviousMilestone != null
                && previousPreviousMilestone.getAchievedAt() != null) {

            previousStartDate =
                    previousPreviousMilestone
                            .getAchievedAt()
                            .toLocalDate()
                            .plusDays(1);
        }

        if (previousEndDate.isBefore(
                previousStartDate
        )) {

            return PreviousExpensePeriod.empty();
        }

        List<ExpenseCategorySummaryResponse> summaries =
                transactionMapper.findVariableExpenseSummary(
                        userId,
                        previousStartDate,
                        previousEndDate
                );

        long days =
                calculateInclusiveDays(
                        previousStartDate,
                        previousEndDate
                );

        return new PreviousExpensePeriod(
                previousStartDate,
                previousEndDate,
                days,
                summaries
        );
    }

    private long calculateInclusiveDays(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate == null
                || endDate == null
                || endDate.isBefore(startDate)) {

            return 0L;
        }

        return ChronoUnit.DAYS.between(
                startDate,
                endDate
        ) + 1L;
    }

    private long calculateDailyAverage(
            long totalExpense,
            long days
    ) {

        if (totalExpense <= 0L
                || days <= 0L) {

            return 0L;
        }

        return Math.round(
                totalExpense / (double) days
        );
    }

    private Map<ExpenseCategory, Long> aggregateByCategory(
            List<ExpenseCategorySummaryResponse> summaries
    ) {

        Map<ExpenseCategory, Long> amounts =
                new EnumMap<>(
                        ExpenseCategory.class
                );

        for (ExpenseCategory category :
                ExpenseCategory.values()) {

            amounts.put(
                    category,
                    0L
            );
        }

        if (summaries == null
                || summaries.isEmpty()) {

            return amounts;
        }

        for (ExpenseCategorySummaryResponse summary :
                summaries) {

            if (summary == null
                    || !StringUtils.hasText(
                    summary.getCategoryName()
            )) {
                continue;
            }

            ExpenseCategory category =
                    findCategory(
                            summary.getCategoryName()
                    );

            long amount =
                    summary.getAmount() == null
                            ? 0L
                            : Math.max(
                            0L,
                            summary.getAmount()
                    );

            amounts.merge(
                    category,
                    amount,
                    Long::sum
            );
        }

        return amounts;
    }

    private ExpenseCategory findCategory(
            String categoryName
    ) {

        String normalizedName =
                categoryName.trim();

        for (ExpenseCategory category :
                ExpenseCategory.values()) {

            if (category.getDisplayName()
                    .equals(normalizedName)) {

                return category;
            }
        }

        return ExpenseCategory.ETC;
    }

    private List<MilestoneReportAiRequest.CategoryExpense>
    buildCategories(
            Map<ExpenseCategory, Long> currentAmounts,
            Map<ExpenseCategory, Long> previousAmounts,
            long totalExpense,
            long currentDays,
            long previousDays
    ) {

        List<MilestoneReportAiRequest.CategoryExpense> categories =
                new ArrayList<>();

        for (ExpenseCategory category :
                ExpenseCategory.values()) {

            long currentAmount =
                    currentAmounts.getOrDefault(
                            category,
                            0L
                    );

            long previousAmount =
                    previousAmounts.getOrDefault(
                            category,
                            0L
                    );

            double proportion =
                    calculateProportion(
                            currentAmount,
                            totalExpense
                    );

            double currentDailyAverage =
                    calculateDailyAverageDouble(
                            currentAmount,
                            currentDays
                    );

            double previousDailyAverage =
                    calculateDailyAverageDouble(
                            previousAmount,
                            previousDays
                    );

            Double changeRate =
                    calculateChangeRate(
                            currentDailyAverage,
                            previousDailyAverage
                    );

            categories.add(
                    MilestoneReportAiRequest
                            .CategoryExpense
                            .builder()
                            .category(
                                    category.getDisplayName()
                            )
                            .amount(currentAmount)
                            .proportion(proportion)
                            .changeRate(changeRate)
                            .build()
            );
        }

        categories.sort(
                Comparator.comparingLong(
                        MilestoneReportAiRequest
                                .CategoryExpense
                                ::getAmount
                ).reversed()
        );

        return categories;
    }

    private double calculateProportion(
            long amount,
            long totalExpense
    ) {

        if (amount <= 0L
                || totalExpense <= 0L) {

            return 0.0;
        }

        return Math.round(
                amount * 10000.0 / totalExpense
        ) / 100.0;
    }

    private double calculateDailyAverageDouble(
            long amount,
            long days
    ) {

        if (amount <= 0L
                || days <= 0L) {

            return 0.0;
        }

        return amount / (double) days;
    }

    private Double calculateChangeRate(
            double currentDailyAverage,
            double previousDailyAverage
    ) {

        if (previousDailyAverage <= 0.0) {
            return null;
        }

        return Math.round(
                (
                        (currentDailyAverage
                                - previousDailyAverage)
                                * 10000.0
                                / previousDailyAverage
                )
        ) / 100.0;
    }

    private MilestoneReportAiRequest.ExpenseSummary
    buildEmptyExpenseSummary() {

        return MilestoneReportAiRequest
                .ExpenseSummary
                .builder()
                .startDate(null)
                .endDate(null)
                .totalExpense(0L)
                .dailyAverageExpense(0L)
                .categories(
                        buildEmptyCategories()
                )
                .build();
    }

    private MilestoneReportAiRequest.ExpenseSummary
    buildEmptyExpenseSummary(
            LocalDate startDate,
            LocalDate endDate
    ) {

        return MilestoneReportAiRequest
                .ExpenseSummary
                .builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalExpense(0L)
                .dailyAverageExpense(0L)
                .categories(
                        buildEmptyCategories()
                )
                .build();
    }

    private List<MilestoneReportAiRequest.CategoryExpense>
    buildEmptyCategories() {

        List<MilestoneReportAiRequest.CategoryExpense> categories =
                new ArrayList<>();

        for (ExpenseCategory category :
                ExpenseCategory.values()) {

            categories.add(
                    MilestoneReportAiRequest
                            .CategoryExpense
                            .builder()
                            .category(
                                    category.getDisplayName()
                            )
                            .amount(0L)
                            .proportion(0.0)
                            .changeRate(null)
                            .build()
            );
        }

        return categories;
    }

    private LocalDate resolveReportEndDate(
            Goal goal,
            Milestone milestone
    ) {

        if (milestone != null
                && milestone.getAchievedAt() != null) {

            return milestone
                    .getAchievedAt()
                    .toLocalDate();
        }

        return calculateTargetDate(
                goal,
                milestone == null
                        ? null
                        : milestone.getMilestonePercentage()
        );
    }

    private LocalDate calculateTargetDate(
            Goal goal,
            Integer percentage
    ) {

        if (goal == null
                || goal.getStartDate() == null
                || goal.getGoalDate() == null
                || percentage == null) {

            return goal == null
                    ? null
                    : goal.getStartDate();
        }

        long totalDays =
                ChronoUnit.DAYS.between(
                        goal.getStartDate(),
                        goal.getGoalDate()
                );

        if (totalDays <= 0L) {
            return goal.getStartDate();
        }

        int normalizedPercentage =
                Math.max(
                        0,
                        Math.min(
                                100,
                                percentage
                        )
                );

        long offsetDays =
                Math.round(
                        totalDays
                                * normalizedPercentage
                                / 100.0
                );

        offsetDays =
                Math.max(
                        0L,
                        Math.min(
                                totalDays,
                                offsetDays
                        )
                );

        return goal.getStartDate()
                .plusDays(offsetDays);
    }

    private record PreviousExpensePeriod(
            LocalDate startDate,
            LocalDate endDate,
            long days,
            List<ExpenseCategorySummaryResponse> summaries
    ) {

        private static PreviousExpensePeriod empty() {

            return new PreviousExpensePeriod(
                    null,
                    null,
                    0L,
                    List.of()
            );
        }
    }
}