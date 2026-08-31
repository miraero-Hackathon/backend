package org.jejuro.miraero.domain.goal.milestone.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneReportAiRequest {

    private GoalInfo goal;
    private MilestoneInfo milestone;
    private ExpenseSummary expenseSummary;

    /** 데이터가 없으면 null. 프롬프트가 이 경우 언급하지 않도록 지시한다 */
    private PaceInfo pace;

    private SavingSummary savingSummary;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalInfo {

        private String goalName;
        private String goalType;
        private Long goalAmount;
        private Long startAmount;
        private LocalDate startDate;
        private LocalDate goalDate;
    }

    /**
     * 목표 진행 속도.
     *
     * 기존에는 AI가 지출 데이터만 보고 진행 상황을 유추했으나,
     * 이 값이 들어가면서 사실에 근거해 판단하게 된다.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaceInfo {

        private Long currentAmount;
        private Long expectedAmount;
        private Long differenceAmount;

        /** AHEAD / ON_TRACK / BEHIND */
        private String paceStatus;
    }

    /**
     * 구간 내 적립 이력 요약.
     *
     * 지출이 "얼마 썼는가"라면 이쪽은 "약속을 얼마나 지켰는가"를 나타낸다.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavingSummary {

        private Long attemptCount;
        private Long successCount;
        private Long partialCount;
        private Long failedCount;
        private Long totalSavedAmount;
        private Long maxStreak;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneInfo {

        private Integer percentage;
        private Long milestoneAmount;
        private LocalDate targetDate;
        private LocalDate achievedAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseSummary {

        private LocalDate startDate;
        private LocalDate endDate;
        private Long totalExpense;
        private Long dailyAverageExpense;

        @Builder.Default
        private List<CategoryExpense> categories = new ArrayList<>();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryExpense {

        private String category;
        private Long amount;
        private Double proportion;
        private Double changeRate;
    }
}