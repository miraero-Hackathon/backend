package org.jejuro.miraero.domain.utilitypayment.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.service.CreditScoreService;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityPaymentState;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityType;
import org.jejuro.miraero.domain.utilitypayment.mapper.UtilityPaymentStateMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 유저 한 명 + 항목 하나(예: userId=42, TELECOM)를 판정하는 단위 로직.
@Component
@RequiredArgsConstructor
public class UtilityPaymentExecutor {
    private static final int STREAK_INTERVAL_MONTHS = 6;
    private static final int OVERDUE_INTERVAL_MONTHS = 3;

    private static final int STREAK_DELTA = 3;
    private static final int OVERDUE_DELTA = -3;

    private final TransactionMapper transactionMapper;
    private final UtilityPaymentStateMapper utilityPaymentStateMapper;
    private final CreditScoreService creditScoreService;

    @Transactional
    public void execute(Long userId, UtilityType utilityType, YearMonth targetMonth) {

        // 1) targetMonth(예: 2026년 9월) 한 달 동안, 이 항목 패턴과 일치하는 거래가 있었는지 확인
        LocalDateTime monthStart = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        boolean paid = transactionMapper.existsPaymentInPeriod(
                userId, utilityType.getMerchantNamePatterns(), monthStart, monthEnd
        );

        // 2) 이 유저+항목의 지금까지 카운터 조회. 처음이면 0부터 시작.
        UtilityPaymentState state = utilityPaymentStateMapper.findByUserIdAndType(userId, utilityType);
        if (state == null) {
            state = UtilityPaymentState.builder()
                    .userId(userId)
                    .utilityType(utilityType)
                    .build();
        }

        LocalDateTime occurredAt = targetMonth.atEndOfMonth().atStartOfDay();

        if (paid) {
            state.markPaid(); // 연속납부 +1, 연속미납 0으로 리셋

            // 나머지 연산으로 "6개월째, 12개월째, 18개월째..."마다 정확히 한 번씩만 걸림
            if (state.getConsecutivePaidMonths() % STREAK_INTERVAL_MONTHS == 0) {
                creditScoreService.applyEvent(
                        userId,
                        STREAK_DELTA,
                        CreditScoreReasonCode.UTILITY_PAYMENT_STREAK,
                        utilityType.getDisplayName() + " " + state.getConsecutivePaidMonths() + "개월 연속 납부",
                        occurredAt
                );
            }
        } else {
            state.markMissed(); // 연속납부 0으로 리셋, 연속미납 +1

            // 5개 항목 전부 연체 차감 대상 (나머지 연산으로 3개월째, 6개월째... 마다 반복)
            if (state.getConsecutiveMissedMonths() % OVERDUE_INTERVAL_MONTHS == 0) {
                creditScoreService.applyEvent(
                        userId,
                        OVERDUE_DELTA,
                        CreditScoreReasonCode.UTILITY_PAYMENT_OVERDUE,
                        utilityType.getDisplayName() + " " + state.getConsecutiveMissedMonths() + "개월 연속 미납",
                        occurredAt
                );
            }
        }

        // 3) 이번 달 판정 결과 저장
        utilityPaymentStateMapper.upsert(state);
    }
}
