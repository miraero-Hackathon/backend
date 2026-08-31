package org.jejuro.miraero.domain.aicoach.context;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class AiCoachFinancialContext {

    private List<ActiveGoal> activeGoals;
    private Long totalAssets;
    private Long totalDebt;
    private Long monthlyIncome;
    private Long currentMonthTotalExpense;
    private List<CategoryExpense> currentMonthCategoryExpenses;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveGoal {

        private String goalName;
        private Long goalAmount;
        private LocalDate goalDate;
    }

    @Getter
    @AllArgsConstructor
    public static class CategoryExpense {

        private String categoryName;
        private Long amount;
    }
}
