package org.jejuro.miraero.domain.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.transaction.domain.CategoryMonthExpenseQueryResult;
import org.jejuro.miraero.domain.transaction.domain.CategoryThreeMonthExpenseQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseAnalysisSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;
import org.jejuro.miraero.domain.transaction.mapper.ExpenseAnalysisMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseAnalysisServiceImplTest {

    @Mock
    private ExpenseAnalysisMapper mapper;
    @Test
    void getDashboard_returnsThreeMonthAveragesAndMonthChanges() {
        ExpenseAnalysisService service = service();
        when(mapper.findCategoryThreeMonthExpenses(eq(1L), any())).thenReturn(List.of(
                new CategoryThreeMonthExpenseQueryResult(1L, "식비", 660_000L)
        ));

        when(mapper.findCategoryMonthExpenses(eq(1L), any(), any(), any())).thenReturn(List.of(
                new CategoryMonthExpenseQueryResult(1L, "food", 250_000L, 280_000L),
                new CategoryMonthExpenseQueryResult(2L, "cafe", 0L, 0L)
        ));
        ExpenseDashboardResponse response = service.getDashboard(1L, 2026, 7);

        assertEquals("2026-04", response.getCategoryThreeMonthAverages().getStartMonth());
        assertEquals("2026-06", response.getCategoryThreeMonthAverages().getEndMonth());
        assertEquals(220_000L, response.getCategoryThreeMonthAverages().getCategories().get(0).getAverageMonthlyAmount());
        assertEquals(30_000L, response.getCategoryMonthChanges().get(0).getChangeAmount());
        assertEquals(0L, response.getCategoryMonthChanges().get(1).getPreviousMonthAmount());
        assertEquals(0L, response.getCategoryMonthChanges().get(1).getCurrentMonthAmount());
        assertEquals(0L, response.getCategoryMonthChanges().get(1).getChangeAmount());
    }

    @Test
    void getDashboard_usesCompletedThreeMonthRangeExcludingCurrentMonth() {
        ExpenseAnalysisService service = service();
        when(mapper.findCategoryThreeMonthExpenses(eq(1L), any())).thenReturn(Collections.emptyList());

        service.getDashboard(1L, 2026, 7);

        ArgumentCaptor<ExpenseAnalysisSearchCondition> averageConditionCaptor =
                ArgumentCaptor.forClass(ExpenseAnalysisSearchCondition.class);
        ArgumentCaptor<LocalDateTime> previousMonthStartCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> currentMonthStartCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> currentMonthEndCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);
        verify(mapper).findCategoryThreeMonthExpenses(eq(1L), averageConditionCaptor.capture());
        verify(mapper).findCategoryMonthExpenses(
                eq(1L),
                previousMonthStartCaptor.capture(),
                currentMonthStartCaptor.capture(),
                currentMonthEndCaptor.capture()
        );

        assertEquals(LocalDateTime.of(2026, 4, 1, 0, 0), averageConditionCaptor.getValue().getStartDateTime());
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), averageConditionCaptor.getValue().getEndDateTime());
        assertEquals(LocalDateTime.of(2026, 6, 1, 0, 0), previousMonthStartCaptor.getValue());
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), currentMonthStartCaptor.getValue());
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0), currentMonthEndCaptor.getValue());
    }

    @Test
    void getDashboard_alwaysDividesTotalByThreeWithTruncation() {
        ExpenseAnalysisService service = service();
        when(mapper.findCategoryThreeMonthExpenses(eq(1L), any())).thenReturn(List.of(
                new CategoryThreeMonthExpenseQueryResult(1L, "식비", 100_000L)
        ));

        ExpenseDashboardResponse response = service.getDashboard(1L, 2026, 7);

        assertEquals(33_333L, response.getCategoryThreeMonthAverages().getCategories().get(0).getAverageMonthlyAmount());
    }

    @Test
    void getDashboard_returnsEmptyAverageCategoriesWhenNoPaymentExistsInPeriod() {
        ExpenseAnalysisService service = service();
        when(mapper.findCategoryThreeMonthExpenses(eq(1L), any())).thenReturn(Collections.emptyList());

        ExpenseDashboardResponse response = service.getDashboard(1L, 2026, 1);

        assertEquals("2025-10", response.getCategoryThreeMonthAverages().getStartMonth());
        assertEquals("2025-12", response.getCategoryThreeMonthAverages().getEndMonth());
        assertEquals(0, response.getCategoryThreeMonthAverages().getCategories().size());
    }

    @Test
    void getDashboard_rejectsInvalidMonth() {
        ExpenseAnalysisService service = service();

        assertThrows(BusinessException.class, () -> service.getDashboard(1L, 2026, 13));
    }

    private ExpenseAnalysisService service() {
        return new ExpenseAnalysisServiceImpl(mapper);
    }
}
