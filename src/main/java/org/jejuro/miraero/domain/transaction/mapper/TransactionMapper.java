package org.jejuro.miraero.domain.transaction.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.transaction.domain.TransactionQueryResult;
import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.AvailableMoneyExpenseSummary;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategorySummaryResponse;

public interface TransactionMapper {

    List<TransactionQueryResult> findTransactions(
            @Param("userId") Long userId,
            @Param("condition") TransactionSearchCondition condition
    );

    long countTransactions(
            @Param("userId") Long userId,
            @Param("condition") TransactionSearchCondition condition
    );


    List<LocalDateTime> findLatestSalaryDateTimes(@Param("userId") Long userId, @Param("limit") int limit);

    // 저금통 자동이체는 급여가 들어오는 계좌에서만 허용하므로 그 계좌를 특정한다
    Long findLatestSalaryAccountId(@Param("userId") Long userId);

    AvailableMoneyExpenseSummary findAvailableMoneyExpenseSummary(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 오늘 지출 합산 (아침 8시 기준 24시간 - XML 내부에서 NOW() 기준 계산)
    Long findTodayExpenseSum(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    List<ExpenseCategorySummaryResponse> findVariableExpenseSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 특정 유저가 특정 기간 동안, 주어진 가맹점명 패턴 중 하나라도 일치하는 거래를 했는지 확인.
    boolean existsPaymentInPeriod(
            @Param("userId") Long userId,
            @Param("patterns") List<String> merchantNamePatterns,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 특정 카드로 특정 기간 동안 결제(PAYMENT)한 금액 합계 — 체크카드 사용 실적 판정용
    Long sumPaymentAmountByCardInPeriod(
            @Param("cardId") Long cardId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

}
