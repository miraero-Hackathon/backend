package org.jejuro.miraero.domain.transaction.mapper;

import java.util.List;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.transaction.domain.CategoryMonthExpenseQueryResult;
import org.jejuro.miraero.domain.transaction.domain.CategoryThreeMonthExpenseQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseAnalysisSearchCondition;

public interface ExpenseAnalysisMapper {
    List<CategoryThreeMonthExpenseQueryResult> findCategoryThreeMonthExpenses(@Param("userId") Long userId, @Param("condition") ExpenseAnalysisSearchCondition condition);

    List<CategoryMonthExpenseQueryResult> findCategoryMonthExpenses(
            @Param("userId") Long userId,
            @Param("previousMonthStartDateTime") LocalDateTime previousMonthStartDateTime,
            @Param("currentMonthStartDateTime") LocalDateTime currentMonthStartDateTime,
            @Param("currentMonthEndDateTime") LocalDateTime currentMonthEndDateTime
    );
}
