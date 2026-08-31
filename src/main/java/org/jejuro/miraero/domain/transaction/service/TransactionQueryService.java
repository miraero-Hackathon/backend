package org.jejuro.miraero.domain.transaction.service;

import org.jejuro.miraero.domain.transaction.dto.response.AvailableMoneyExpenseSummary;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionQueryService {
    /**
     * 사용자의 최근 급여 입금 일시를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 가장 최근 급여 거래 일시
     */
    List<LocalDateTime> getLatestSalaryDateTimes(Long userId, int limit);

    /**
     * 사용자의 급여가 입금되는 계좌 ID를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 급여 입금 계좌 ID. 급여 거래를 찾지 못하면 null
     */
    Long getSalaryAccountId(Long userId);

    AvailableMoneyExpenseSummary getAvailableMoneyExpenseSummary(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 사용자의 오늘 지출 금액 합계를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 오늘 지출 합계 금액
     */
    Long getTodayExpenseSum(
            Long userId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );



}
