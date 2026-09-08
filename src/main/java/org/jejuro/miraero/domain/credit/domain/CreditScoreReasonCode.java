package org.jejuro.miraero.domain.credit.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CreditScoreReasonCode {

    // --- 2단계: 대출 시뮬레이션 (저축목표를 대출로 미러링) ---
    SHORT_TERM_OVERDUE("단기 연체"),       // 이체 30일 미납. 차감
    LONG_TERM_OVERDUE("장기 연체"),        // 이체 90일 미납. 단기연체보다 크게 차감
    CONSECUTIVE_REPAYMENT("연속 상환"),    // 3개월 이상 연속 정상 납입. 가점
    LOAN_FULLY_REPAID("원금 완납"),        // 미러링된 대출 원금을 전부 갚음. 가점

    // --- 3단계: 통신비/공과금 납부 ---
    // 어떤 공과금 항목인지(통신비/도시가스/수도 등)는 description에 텍스트로 담는다.
    UTILITY_PAYMENT_STREAK("공과금 성실납부"),   // 6개월 이상 연속 납부. 가점
    UTILITY_PAYMENT_OVERDUE("공과금 연체"),     // 3개월 연체. 차감

    // --- 4단계: 체크카드 사용 ---
    CHECK_CARD_USAGE_STREAK("체크카드 사용 실적"); // 월 30만원 이상 6개월 이상 사용. 가점

    private final String displayName;
}
