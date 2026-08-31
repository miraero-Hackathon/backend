package org.jejuro.miraero.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.response.AvailableMoneyExpenseSummary;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionQueryServiceImpl implements TransactionQueryService {

    private final TransactionMapper transactionMapper;

    @Override
    public List<LocalDateTime> getLatestSalaryDateTimes(Long userId, int limit) {
        return transactionMapper.findLatestSalaryDateTimes(userId, limit);
    }

    @Override
    public Long getSalaryAccountId(Long userId) {
        return transactionMapper.findLatestSalaryAccountId(userId);
    }


    @Override
    public AvailableMoneyExpenseSummary getAvailableMoneyExpenseSummary(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        AvailableMoneyExpenseSummary summary =
                transactionMapper.findAvailableMoneyExpenseSummary(
                        userId,
                        startDate,
                        endDate
                );

        if (summary == null) {
            return new AvailableMoneyExpenseSummary(0L, 0L);
        }

        return summary;
    }

    @Override
    public Long getTodayExpenseSum(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Long sum = transactionMapper.findTodayExpenseSum(userId,startDateTime,endDateTime);
        return sum == null ? 0L : sum;
    }
}
