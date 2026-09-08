package org.jejuro.miraero.domain.utilitypayment.domain;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 신용점수에 반영되는 5개 공과금/비금융 항목.
// merchantNamePatterns는 거래내역(transaction.merchant_name)을 이 항목으로 인식할 문자열 패턴 목록 —
// LIKE '%패턴%'으로 매칭됨. 지금은 예시값이고, 실제 시드 데이터(다른 팀원이 작성 예정)에 맞춰
// 나중에 이 목록만 고치면 됨 — 판정 로직(Executor)은 안 건드려도 됨.
//
// 5개 항목 전부 6개월 연속 납부 가점 + 3개월 연속 미납 차감 대상으로 동일하게 취급한다
// (2026-09-07 세션 중 확정 — 원래는 도시가스/수도요금만 차감 대상이었으나, 5개 항목 전부
// 동일 규칙으로 바뀌면서 항목별 구분 필드(overduePenaltyApplicable) 자체가 불필요해져 제거함).

@Getter
@RequiredArgsConstructor
public enum UtilityType {

    TELECOM("통신비", List.of("SKT", "KT", "LG유플러스", "KB통신")),
    PENSION("국민연금", List.of("국민연금공단")),
    HEALTH_INSURANCE("건강보험료", List.of("국민건강보험공단")),
    GAS("도시가스", List.of("도시가스")),
    WATER("수도요금", List.of("수도요금", "상수도"));

    private final String displayName;
    private final List<String> merchantNamePatterns;
}
