package org.jejuro.miraero.domain.transaction.service;

import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.domain.ExpenseSimulationCurrentExpense;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseSimulationCategoryRequest;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseSimulationRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseSimulationCategoryResponse;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseSimulationResponse;
import org.jejuro.miraero.domain.transaction.mapper.ExpenseSimulationMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseSimulationServiceImpl implements ExpenseSimulationService {
    private final ExpenseSimulationMapper mapper;
    @Override @Transactional(readOnly = true)
    public ExpenseSimulationResponse simulate(Long userId, ExpenseSimulationRequest request) {
        validate(userId, request);
        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
        List<Long> ids = request.getCategories().stream().map(ExpenseSimulationCategoryRequest::getCategoryId).collect(Collectors.toList());
        List<ExpenseSimulationCurrentExpense> found = mapper.findCurrentExpensesByCategories(userId, ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay(), ids);
        Map<Long, ExpenseSimulationCurrentExpense> current = new HashMap<>();
        for (ExpenseSimulationCurrentExpense value : found == null ? Collections.<ExpenseSimulationCurrentExpense>emptyList() : found) {
            if (current.put(value.getCategoryId(), value) != null) throw invalid();
        }
        if (!current.keySet().containsAll(ids)) throw invalid();
        long[] totals = new long[3];
        List<ExpenseSimulationCategoryResponse> categories = request.getCategories().stream().map(c -> {
            ExpenseSimulationCurrentExpense value = current.get(c.getCategoryId()); long expense = value.getCurrentExpense(); long reduction = Math.max(expense - c.getTargetExpense(), 0L);
            totals[0] = Math.addExact(totals[0], expense); totals[1] = Math.addExact(totals[1], c.getTargetExpense()); totals[2] = Math.addExact(totals[2], reduction);
            return new ExpenseSimulationCategoryResponse(c.getCategoryId(), value.getCategoryName(), expense, c.getTargetExpense(), reduction);
        }).collect(Collectors.toList());
        return new ExpenseSimulationResponse(request.getYear(), request.getMonth(), totals[0], totals[1], totals[2], categories);
    }
    private void validate(Long userId, ExpenseSimulationRequest r) {
        if (userId == null || userId <= 0 || r == null || r.getYear() == null || r.getMonth() == null || r.getMonth() < 1 || r.getMonth() > 12 || r.getCategories() == null || r.getCategories().isEmpty()) throw invalid();
        Set<Long> ids = new HashSet<>(); for (ExpenseSimulationCategoryRequest c : r.getCategories()) if (c == null || c.getCategoryId() == null || c.getCategoryId() <= 0 || c.getTargetExpense() == null || c.getTargetExpense() < 0 || !ids.add(c.getCategoryId())) throw invalid();
    }
    private BusinessException invalid() { return new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE); }
}
