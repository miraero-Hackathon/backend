package org.jejuro.miraero.domain.checkcard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 체크카드 한 장의 월별 사용 실적 상태 (카드 1장당 1행).
// UtilityPaymentState와 같은 "연속 카운터" 패턴이지만, 항목(enum) 대신 카드(card_id) 단위다.
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckCardUsageState {
    private Long checkCardUsageStateId;
    private Long userId;
    private Long cardId;

    private int consecutiveQualifiedMonths;

    // 이번 달 결제합이 임계값(30만원) 이상이었을 때 — 연속충족 개월수 +1
    public void markQualified() {
        this.consecutiveQualifiedMonths += 1;
    }

    // 이번 달 미달이었을 때 — 연속충족 0으로 리셋
    public void markMissed() {
        this.consecutiveQualifiedMonths = 0;
    }
}
