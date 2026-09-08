package org.jejuro.miraero.domain.checkcard.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.card.mapper.CardMapper;
import org.jejuro.miraero.domain.checkcard.domain.CheckCardAwardState;
import org.jejuro.miraero.domain.checkcard.domain.CheckCardUsageState;
import org.jejuro.miraero.domain.checkcard.mapper.CheckCardAwardStateMapper;
import org.jejuro.miraero.domain.checkcard.mapper.CheckCardUsageStateMapper;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.service.CreditScoreService;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 유저 한 명을 판정하는 단위 로직.
// 판정은 카드별(카드마다 연속충족 개월수를 따로 추적), 지급은 유저별(카드 여러 장이 동시에
// 스트릭을 채워도 40점은 딱 한 번만)이라 단위가 달라서, 카드 루프(1)와 유저 레벨 지급 판단(2)이
// 한 메서드 안에 같이 들어간다.
@Component
@RequiredArgsConstructor
public class CheckCardUsageExecutor {

    private static final long QUALIFY_THRESHOLD_AMOUNT = 300_000L; // 월 30만원
    private static final int STREAK_TARGET_MONTHS = 6;
    private static final int AWARD_DELTA = 40;

    private final CardMapper cardMapper;
    private final TransactionMapper transactionMapper;
    private final CheckCardUsageStateMapper checkCardUsageStateMapper;
    private final CheckCardAwardStateMapper checkCardAwardStateMapper;
    private final CreditScoreService creditScoreService;

    @Transactional
    public void execute(Long userId, YearMonth targetMonth) {

        LocalDateTime monthStart = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = targetMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Long> cardIds = cardMapper.findCheckCardIdsByUserId(userId);

        boolean anyCardReachedStreak = false;

        // 1) 카드별 판정 — 카드마다 이번 달 결제합이 임계값 이상이면 연속충족 +1, 아니면 리셋
        for (Long cardId : cardIds) {
            Long sum = transactionMapper.sumPaymentAmountByCardInPeriod(cardId, monthStart, monthEnd);

            CheckCardUsageState state = checkCardUsageStateMapper.findByCardId(cardId);
            if (state == null) {
                state = CheckCardUsageState.builder()
                        .userId(userId)
                        .cardId(cardId)
                        .build();
            }

            if (sum != null && sum >= QUALIFY_THRESHOLD_AMOUNT) {
                state.markQualified();
            } else {
                state.markMissed();
            }

            checkCardUsageStateMapper.upsert(state);

            if (state.getConsecutiveQualifiedMonths() >= STREAK_TARGET_MONTHS) {
                anyCardReachedStreak = true;
            }
        }

        // 2) 유저 레벨 지급 판단 — 카드 중 하나라도 스트릭을 채웠고, 쿨다운(12개월)이 끝났으면
        // 그 즉시 딱 한 번만 지급한다. 2단계(대출 시뮬레이션)의 "6개월 채우면 바로 +50점"과
        // 동일한 타이밍 — 조건 만족 후 유예 기간을 두지 않는다.
        if (!anyCardReachedStreak) {
            return;
        }

        CheckCardAwardState awardState = checkCardAwardStateMapper.findByUserId(userId);
        if (awardState == null) {
            awardState = CheckCardAwardState.builder()
                    .userId(userId)
                    .build();
        }

        if (!awardState.canAward(targetMonth)) {
            return;
        }

        creditScoreService.applyEvent(
                userId,
                AWARD_DELTA,
                CreditScoreReasonCode.CHECK_CARD_USAGE_STREAK,
                "체크카드 " + STREAK_TARGET_MONTHS + "개월 이상 연속 30만원 이상 사용",
                targetMonth.atEndOfMonth().atStartOfDay()
        );

        awardState.markAwarded(targetMonth);
        checkCardAwardStateMapper.upsert(awardState);
    }
}
