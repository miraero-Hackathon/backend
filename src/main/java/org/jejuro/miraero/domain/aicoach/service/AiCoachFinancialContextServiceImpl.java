package org.jejuro.miraero.domain.aicoach.service;

import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.context.AiCoachFinancialContext;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachFinancialContextMapper;
import org.jejuro.miraero.domain.transaction.dto.response.CategoryMonthChangeResponse;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;
import org.jejuro.miraero.domain.transaction.service.ExpenseAnalysisService;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiCoachFinancialContextServiceImpl implements AiCoachFinancialContextService {

    private final AiCoachFinancialContextMapper aiCoachFinancialContextMapper;
    private final UserMapper userMapper;
    private final ExpenseAnalysisService expenseAnalysisService;

    @Override
    public AiCoachFinancialContext getFinancialContext(Long userId) {
        User user = getUser(userId);
        YearMonth currentMonth = YearMonth.now();
        ExpenseDashboardResponse expenseDashboard = expenseAnalysisService.getDashboard(
                userId,
                currentMonth.getYear(),
                currentMonth.getMonthValue()
        );
        List<AiCoachFinancialContext.CategoryExpense> categoryExpenses = toCategoryExpenses(
                expenseDashboard.getCategoryMonthChanges()
        );

        return AiCoachFinancialContext.builder()
                .activeGoals(aiCoachFinancialContextMapper.findActiveGoalsByUserId(userId))
                .totalAssets(getTotalAssets(userId))
                .totalDebt(aiCoachFinancialContextMapper.sumLoanRemainingAmountsByUserId(userId))
                .monthlyIncome(user.getMonthlyIncome())
                .currentMonthTotalExpense(calculateTotalExpense(categoryExpenses))
                .currentMonthCategoryExpenses(categoryExpenses)
                .build();
    }

    private User getUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
        return user;
    }

    private Long getTotalAssets(Long userId) {
        return Math.addExact(
                aiCoachFinancialContextMapper.sumAccountBalancesByUserId(userId),
                aiCoachFinancialContextMapper.sumMoneyBoxBalancesByUserId(userId)
        );
    }

    private List<AiCoachFinancialContext.CategoryExpense> toCategoryExpenses(
            List<CategoryMonthChangeResponse> categoryMonthChanges
    ) {
        return categoryMonthChanges.stream()
                .map(category -> new AiCoachFinancialContext.CategoryExpense(
                        category.getCategoryName(),
                        category.getCurrentMonthAmount()
                ))
                .toList();
    }

    private Long calculateTotalExpense(List<AiCoachFinancialContext.CategoryExpense> categoryExpenses) {
        return categoryExpenses.stream()
                .map(AiCoachFinancialContext.CategoryExpense::getAmount)
                .reduce(0L, Math::addExact);
    }
}
