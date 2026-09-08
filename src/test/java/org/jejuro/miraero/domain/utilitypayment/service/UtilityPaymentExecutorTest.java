package org.jejuro.miraero.domain.utilitypayment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.time.YearMonth;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.service.CreditScoreService;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityPaymentState;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityType;
import org.jejuro.miraero.domain.utilitypayment.mapper.UtilityPaymentStateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UtilityPaymentExecutorTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private UtilityPaymentStateMapper utilityPaymentStateMapper;

    @Mock
    private CreditScoreService creditScoreService;

    @InjectMocks
    private UtilityPaymentExecutor utilityPaymentExecutor;

    private static final Long USER_ID = 1L;

    private UtilityPaymentState.UtilityPaymentStateBuilder stateBuilder(UtilityType type) {
        return UtilityPaymentState.builder().userId(USER_ID).utilityType(type);
    }

    // ---------- 납부(가점) 쪽 — 6개월마다 반복 ----------

    @Test
    @DisplayName("납부했지만 아직 6개월 배수가 아니면 가점하지 않는다")
    void execute_paid_notMultipleOf6_noBonus() {
        // given: 이번 달 납부하면 3개월째가 됨 (2 → 3)
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(true);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.TELECOM))
                .willReturn(stateBuilder(UtilityType.TELECOM).consecutivePaidMonths(2).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.TELECOM, targetMonth);

        // then
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());
    }

    @Test
    @DisplayName("연속 납부가 정확히 6개월째가 되면 +3점 가점한다")
    void execute_paid_reaches6Months_appliesBonus() {
        // given: 5개월째 납부 상태에서 이번 달도 납부 → 6개월째
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(true);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.TELECOM))
                .willReturn(stateBuilder(UtilityType.TELECOM).consecutivePaidMonths(5).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.TELECOM, targetMonth);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID),
                eq(3),
                eq(CreditScoreReasonCode.UTILITY_PAYMENT_STREAK),
                eq("통신비 6개월 연속 납부"),
                eq(targetMonth.atEndOfMonth().atStartOfDay())
        );
    }

    @Test
    @DisplayName("12개월째(6의 배수)에도 다시 가점한다 — fired 플래그가 아니라 반복 발생")
    void execute_paid_reaches12Months_appliesBonusAgain() {
        // given: 11개월째 납부 상태에서 이번 달도 납부 → 12개월째
        YearMonth targetMonth = YearMonth.of(2027, 2);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(true);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.TELECOM))
                .willReturn(stateBuilder(UtilityType.TELECOM).consecutivePaidMonths(11).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.TELECOM, targetMonth);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID),
                eq(3),
                eq(CreditScoreReasonCode.UTILITY_PAYMENT_STREAK),
                eq("통신비 12개월 연속 납부"),
                any()
        );
    }

    @Test
    @DisplayName("이번 달 납부하면 연속 미납 카운트가 0으로 리셋된다")
    void execute_paid_resetsMissedStreak() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(true);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.GAS))
                .willReturn(stateBuilder(UtilityType.GAS).consecutivePaidMonths(0).consecutiveMissedMonths(2).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.GAS, targetMonth);

        // then
        ArgumentCaptor<UtilityPaymentState> captor = ArgumentCaptor.forClass(UtilityPaymentState.class);
        verify(utilityPaymentStateMapper).upsert(captor.capture());
        assertEquals(1, captor.getValue().getConsecutivePaidMonths());
        assertEquals(0, captor.getValue().getConsecutiveMissedMonths());
    }

    // ---------- 미납(차감) 쪽 — 5개 항목 전부 대상, 3개월마다 반복 ----------

    @Test
    @DisplayName("도시가스를 3개월 연속 미납하면 -3점 차감한다")
    void execute_missed_gas_reaches3Months_appliesPenalty() {
        // given: 2개월째 미납 상태에서 이번 달도 미납 → 3개월째
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(false);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.GAS))
                .willReturn(stateBuilder(UtilityType.GAS).consecutiveMissedMonths(2).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.GAS, targetMonth);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID),
                eq(-3),
                eq(CreditScoreReasonCode.UTILITY_PAYMENT_OVERDUE),
                eq("도시가스 3개월 연속 미납"),
                eq(targetMonth.atEndOfMonth().atStartOfDay())
        );
    }

    @Test
    @DisplayName("수도요금을 3개월 연속 미납해도 -3점 차감한다")
    void execute_missed_water_reaches3Months_appliesPenalty() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(false);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.WATER))
                .willReturn(stateBuilder(UtilityType.WATER).consecutiveMissedMonths(2).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.WATER, targetMonth);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID), eq(-3), eq(CreditScoreReasonCode.UTILITY_PAYMENT_OVERDUE), any(), any()
        );
    }

    @Test
    @DisplayName("통신비를 3개월 연속 미납하면 -3점 차감한다 (5개 항목 전부 차감 대상)")
    void execute_missed_telecom_appliesPenalty() {
        // given: 2026-09-07 세션 확정 — 통신비도 도시가스/수도요금과 동일하게 차감 대상
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(false);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.TELECOM))
                .willReturn(stateBuilder(UtilityType.TELECOM).consecutiveMissedMonths(2).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.TELECOM, targetMonth);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID),
                eq(-3),
                eq(CreditScoreReasonCode.UTILITY_PAYMENT_OVERDUE),
                eq("통신비 3개월 연속 미납"),
                eq(targetMonth.atEndOfMonth().atStartOfDay())
        );
    }

    @Test
    @DisplayName("국민연금을 3개월 연속 미납하면 -3점 차감한다")
    void execute_missed_pension_appliesPenalty() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(false);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.PENSION))
                .willReturn(stateBuilder(UtilityType.PENSION).consecutiveMissedMonths(2).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.PENSION, targetMonth);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID), eq(-3), eq(CreditScoreReasonCode.UTILITY_PAYMENT_OVERDUE), any(), any()
        );
    }

    @Test
    @DisplayName("건강보험료를 3개월 연속 미납하면 -3점 차감한다")
    void execute_missed_healthInsurance_appliesPenalty() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(false);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.HEALTH_INSURANCE))
                .willReturn(stateBuilder(UtilityType.HEALTH_INSURANCE).consecutiveMissedMonths(2).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.HEALTH_INSURANCE, targetMonth);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID), eq(-3), eq(CreditScoreReasonCode.UTILITY_PAYMENT_OVERDUE), any(), any()
        );
    }

    @Test
    @DisplayName("이번 달 미납하면 연속 납부 카운트가 0으로 리셋된다")
    void execute_missed_resetsPaidStreak() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(false);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.TELECOM))
                .willReturn(stateBuilder(UtilityType.TELECOM).consecutivePaidMonths(5).consecutiveMissedMonths(0).build());

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.TELECOM, targetMonth);

        // then
        ArgumentCaptor<UtilityPaymentState> captor = ArgumentCaptor.forClass(UtilityPaymentState.class);
        verify(utilityPaymentStateMapper).upsert(captor.capture());
        assertEquals(0, captor.getValue().getConsecutivePaidMonths());
        assertEquals(1, captor.getValue().getConsecutiveMissedMonths());
    }

    // ---------- 거래 조회 기간 계산 ----------

    @Test
    @DisplayName("해당 항목의 가맹점명 패턴과, targetMonth의 1일~다음달 1일 구간으로 거래를 조회한다")
    void execute_queriesTransactionWithCorrectMerchantPatternsAndPeriod() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 2); // 28일까지 있는 짧은 달로 경계 계산 확인
        given(transactionMapper.existsPaymentInPeriod(any(), anyList(), any(), any()))
                .willReturn(true);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.TELECOM))
                .willReturn(null);

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.TELECOM, targetMonth);

        // then
        verify(transactionMapper).existsPaymentInPeriod(
                eq(USER_ID),
                eq(UtilityType.TELECOM.getMerchantNamePatterns()),
                eq(LocalDateTime.of(2026, 2, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 3, 1, 0, 0))
        );
    }

    @Test
    @DisplayName("처음 판정하는 유저·항목(상태 없음)은 0개월째부터 시작한다")
    void execute_firstTimeState_startsFromZero() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(transactionMapper.existsPaymentInPeriod(eq(USER_ID), anyList(), any(), any()))
                .willReturn(true);
        given(utilityPaymentStateMapper.findByUserIdAndType(USER_ID, UtilityType.TELECOM))
                .willReturn(null);

        // when
        utilityPaymentExecutor.execute(USER_ID, UtilityType.TELECOM, targetMonth);

        // then
        ArgumentCaptor<UtilityPaymentState> captor = ArgumentCaptor.forClass(UtilityPaymentState.class);
        verify(utilityPaymentStateMapper).upsert(captor.capture());
        assertEquals(1, captor.getValue().getConsecutivePaidMonths());
        assertEquals(USER_ID, captor.getValue().getUserId());
        assertEquals(UtilityType.TELECOM, captor.getValue().getUtilityType());
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());
    }
}
