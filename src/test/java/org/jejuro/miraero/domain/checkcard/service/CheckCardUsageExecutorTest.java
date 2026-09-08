package org.jejuro.miraero.domain.checkcard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.jejuro.miraero.domain.card.mapper.CardMapper;
import org.jejuro.miraero.domain.checkcard.domain.CheckCardAwardState;
import org.jejuro.miraero.domain.checkcard.domain.CheckCardUsageState;
import org.jejuro.miraero.domain.checkcard.mapper.CheckCardAwardStateMapper;
import org.jejuro.miraero.domain.checkcard.mapper.CheckCardUsageStateMapper;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.service.CreditScoreService;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckCardUsageExecutorTest {

    @Mock
    private CardMapper cardMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private CheckCardUsageStateMapper checkCardUsageStateMapper;

    @Mock
    private CheckCardAwardStateMapper checkCardAwardStateMapper;

    @Mock
    private CreditScoreService creditScoreService;

    @InjectMocks
    private CheckCardUsageExecutor checkCardUsageExecutor;

    private static final Long USER_ID = 1L;
    private static final Long CARD_ID = 100L;
    private static final long THRESHOLD = 300_000L;

    private CheckCardUsageState.CheckCardUsageStateBuilder usageStateBuilder() {
        return CheckCardUsageState.builder().userId(USER_ID).cardId(CARD_ID);
    }

    // ---------- 카드별 판정 ----------

    @Test
    @DisplayName("체크카드가 한 장도 없는 유저는 아무 것도 하지 않고 안전하게 끝난다")
    void execute_noCards_doesNothing() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of());

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());
        verify(checkCardUsageStateMapper, never()).upsert(any());
        verify(checkCardAwardStateMapper, never()).upsert(any());
    }

    @Test
    @DisplayName("이번 달 결제합이 30만원 이상이면 연속충족이 +1 된다")
    void execute_paymentAboveThreshold_marksQualified() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(CARD_ID));
        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(CARD_ID), any(), any()))
                .willReturn(THRESHOLD);
        given(checkCardUsageStateMapper.findByCardId(CARD_ID))
                .willReturn(usageStateBuilder().consecutiveQualifiedMonths(2).build());

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        ArgumentCaptor<CheckCardUsageState> captor = ArgumentCaptor.forClass(CheckCardUsageState.class);
        verify(checkCardUsageStateMapper).upsert(captor.capture());
        assertEquals(3, captor.getValue().getConsecutiveQualifiedMonths());
    }

    @Test
    @DisplayName("이번 달 결제합이 30만원 미만이면 연속충족이 0으로 리셋된다")
    void execute_paymentBelowThreshold_marksMissed() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(CARD_ID));
        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(CARD_ID), any(), any()))
                .willReturn(THRESHOLD - 1);
        given(checkCardUsageStateMapper.findByCardId(CARD_ID))
                .willReturn(usageStateBuilder().consecutiveQualifiedMonths(4).build());

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        ArgumentCaptor<CheckCardUsageState> captor = ArgumentCaptor.forClass(CheckCardUsageState.class);
        verify(checkCardUsageStateMapper).upsert(captor.capture());
        assertEquals(0, captor.getValue().getConsecutiveQualifiedMonths());
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());
    }

    @Test
    @DisplayName("결제합이 정확히 30만원이면 충족으로 처리한다 (경계값)")
    void execute_paymentExactlyThreshold_qualifies() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(CARD_ID));
        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(CARD_ID), any(), any()))
                .willReturn(THRESHOLD);
        given(checkCardUsageStateMapper.findByCardId(CARD_ID)).willReturn(null);

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        ArgumentCaptor<CheckCardUsageState> captor = ArgumentCaptor.forClass(CheckCardUsageState.class);
        verify(checkCardUsageStateMapper).upsert(captor.capture());
        assertEquals(1, captor.getValue().getConsecutiveQualifiedMonths());
    }

    @Test
    @DisplayName("이번 달 결제 기간을 targetMonth의 1일~다음 달 1일로 정확히 조회한다")
    void execute_queriesCorrectMonthPeriod() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 2); // 28일까지 있는 짧은 달로 경계 확인
        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(CARD_ID));
        given(transactionMapper.sumPaymentAmountByCardInPeriod(any(), any(), any())).willReturn(0L);
        given(checkCardUsageStateMapper.findByCardId(CARD_ID)).willReturn(null);

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        verify(transactionMapper).sumPaymentAmountByCardInPeriod(
                eq(CARD_ID),
                eq(LocalDateTime.of(2026, 2, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 3, 1, 0, 0))
        );
    }

    // ---------- 유저 레벨 지급 판단 ----------

    @Test
    @DisplayName("연속충족이 6개월 미만이면 카드 조건 자체를 못 채운 것이므로 지급하지 않는다")
    void execute_streakUnder6_noAward() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(CARD_ID));
        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(CARD_ID), any(), any()))
                .willReturn(THRESHOLD);
        given(checkCardUsageStateMapper.findByCardId(CARD_ID))
                .willReturn(usageStateBuilder().consecutiveQualifiedMonths(4).build()); // 이번 달 반영되면 5개월째

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());
        verify(checkCardAwardStateMapper, never()).upsert(any());
    }

    @Test
    @DisplayName("연속충족이 6개월째가 되면 그 즉시 +40점 지급한다 (최초 지급, 유예 없음)")
    void execute_streakReaches6_awardsImmediately() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(CARD_ID));
        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(CARD_ID), any(), any()))
                .willReturn(THRESHOLD);
        given(checkCardUsageStateMapper.findByCardId(CARD_ID))
                .willReturn(usageStateBuilder().consecutiveQualifiedMonths(5).build()); // 이번 달 반영되면 6개월째
        given(checkCardAwardStateMapper.findByUserId(USER_ID)).willReturn(null);

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID),
                eq(40),
                eq(CreditScoreReasonCode.CHECK_CARD_USAGE_STREAK),
                eq("체크카드 6개월 이상 연속 30만원 이상 사용"),
                eq(targetMonth.atEndOfMonth().atStartOfDay())
        );

        ArgumentCaptor<CheckCardAwardState> captor = ArgumentCaptor.forClass(CheckCardAwardState.class);
        verify(checkCardAwardStateMapper).upsert(captor.capture());
        assertEquals(targetMonth.atDay(1), captor.getValue().getLastAwardedYearMonth());
    }

    @Test
    @DisplayName("이미 지급받은 지 12개월이 안 지났으면 조건을 계속 채워도 재지급하지 않는다")
    void execute_withinCooldown_noReAward() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(CARD_ID));
        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(CARD_ID), any(), any()))
                .willReturn(THRESHOLD);
        given(checkCardUsageStateMapper.findByCardId(CARD_ID))
                .willReturn(usageStateBuilder().consecutiveQualifiedMonths(10).build());
        // 6개월 전에 지급받은 상태 (쿨다운 12개월 미경과)
        given(checkCardAwardStateMapper.findByUserId(USER_ID))
                .willReturn(CheckCardAwardState.builder()
                        .userId(USER_ID)
                        .lastAwardedYearMonth(targetMonth.minusMonths(6).atDay(1))
                        .build());

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());
        verify(checkCardAwardStateMapper, never()).upsert(any());
    }

    @Test
    @DisplayName("마지막 지급으로부터 정확히 12개월이 지나면 재지급한다")
    void execute_cooldownExpired_reAwards() {
        // given
        YearMonth targetMonth = YearMonth.of(2026, 8);
        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(CARD_ID));
        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(CARD_ID), any(), any()))
                .willReturn(THRESHOLD);
        given(checkCardUsageStateMapper.findByCardId(CARD_ID))
                .willReturn(usageStateBuilder().consecutiveQualifiedMonths(20).build());
        // 정확히 12개월 전에 지급받은 상태
        given(checkCardAwardStateMapper.findByUserId(USER_ID))
                .willReturn(CheckCardAwardState.builder()
                        .userId(USER_ID)
                        .lastAwardedYearMonth(targetMonth.minusMonths(12).atDay(1))
                        .build());

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID), eq(40), eq(CreditScoreReasonCode.CHECK_CARD_USAGE_STREAK), any(), any()
        );
    }

    @Test
    @DisplayName("카드 여러 장이 동시에 6개월 스트릭을 채워도 40점은 딱 한 번만 지급한다")
    void execute_multipleCardsReachStreak_awardsOnlyOnce() {
        // given
        Long cardId1 = 100L;
        Long cardId2 = 200L;
        YearMonth targetMonth = YearMonth.of(2026, 8);

        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(cardId1, cardId2));

        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(cardId1), any(), any()))
                .willReturn(THRESHOLD);
        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(cardId2), any(), any()))
                .willReturn(THRESHOLD);

        given(checkCardUsageStateMapper.findByCardId(cardId1))
                .willReturn(CheckCardUsageState.builder().userId(USER_ID).cardId(cardId1)
                        .consecutiveQualifiedMonths(5).build());
        given(checkCardUsageStateMapper.findByCardId(cardId2))
                .willReturn(CheckCardUsageState.builder().userId(USER_ID).cardId(cardId2)
                        .consecutiveQualifiedMonths(5).build());

        given(checkCardAwardStateMapper.findByUserId(USER_ID)).willReturn(null);

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        verify(creditScoreService, times(1))
                .applyEvent(eq(USER_ID), eq(40), eq(CreditScoreReasonCode.CHECK_CARD_USAGE_STREAK), any(), any());
        // 카드 상태는 각각 upsert되지만(2번), 지급 상태는 딱 1번만 upsert됨
        verify(checkCardUsageStateMapper, times(2)).upsert(any());
        verify(checkCardAwardStateMapper, times(1)).upsert(any());
    }

    @Test
    @DisplayName("카드 여러 장 중 하나만 6개월 스트릭을 채워도 지급한다")
    void execute_onlyOneOfMultipleCardsReachesStreak_awards() {
        // given
        Long cardId1 = 100L;
        Long cardId2 = 200L;
        YearMonth targetMonth = YearMonth.of(2026, 8);

        given(cardMapper.findCheckCardIdsByUserId(USER_ID)).willReturn(List.of(cardId1, cardId2));

        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(cardId1), any(), any()))
                .willReturn(THRESHOLD);
        given(transactionMapper.sumPaymentAmountByCardInPeriod(eq(cardId2), any(), any()))
                .willReturn(0L);

        given(checkCardUsageStateMapper.findByCardId(cardId1))
                .willReturn(CheckCardUsageState.builder().userId(USER_ID).cardId(cardId1)
                        .consecutiveQualifiedMonths(5).build());
        given(checkCardUsageStateMapper.findByCardId(cardId2))
                .willReturn(CheckCardUsageState.builder().userId(USER_ID).cardId(cardId2)
                        .consecutiveQualifiedMonths(3).build());

        given(checkCardAwardStateMapper.findByUserId(USER_ID)).willReturn(null);

        // when
        checkCardUsageExecutor.execute(USER_ID, targetMonth);

        // then
        verify(creditScoreService, times(1))
                .applyEvent(eq(USER_ID), eq(40), eq(CreditScoreReasonCode.CHECK_CARD_USAGE_STREAK), any(), any());
    }
}
