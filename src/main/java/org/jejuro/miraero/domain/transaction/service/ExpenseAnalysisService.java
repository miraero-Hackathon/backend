package org.jejuro.miraero.domain.transaction.service;

import org.jejuro.miraero.domain.transaction.dto.response.ExpenseDashboardResponse;

public interface ExpenseAnalysisService {
    ExpenseDashboardResponse getDashboard(Long userId, Integer year, Integer month);
}
