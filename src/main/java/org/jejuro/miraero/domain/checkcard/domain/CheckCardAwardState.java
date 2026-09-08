package org.jejuro.miraero.domain.checkcard.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 유저 1명의 체크카드 가점 지급 이력 (유저당 1행).
// 카드는 여러 장이어도 지급은 유저당 한 번뿐이라, 판정 상태(CheckCardUsageState)와 분리해서 관리한다.
//
// 지급은 조건(카드 하나라도 연속 6개월 이상 충족) + 쿨다운(마지막 지급 후 12개월)을 통과하는 즉시
// 나간다 — 2단계(대출 시뮬레이션)의 "6개월 채우면 바로 +50점" 패턴과 동일한 타이밍.
//
// DB엔 DATE 컬럼으로 저장하므로 필드 타입은 LocalDate(그 달의 1일)로 두고, 판정 로직과는
// YearMonth로 주고받는다 — MyBatis가 YearMonth를 기본 지원하지 않아서 커스텀 typeHandler를
// 추가하는 대신, LocalDate로 저장하고 경계에서만 변환하는 쪽을 택했다.
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckCardAwardState {

    private static final int AWARD_COOLDOWN_MONTHS = 12;

    private Long checkCardAwardStateId;
    private Long userId;
    private LocalDate lastAwardedYearMonth;

    // 지급 가능 여부 — 한 번도 지급 안 했거나, 마지막 지급으로부터 12개월 이상 지났으면 true
    public boolean canAward(YearMonth targetMonth) {
        if (lastAwardedYearMonth == null) {
            return true;
        }
        YearMonth lastAwarded = YearMonth.from(lastAwardedYearMonth);
        return ChronoUnit.MONTHS.between(lastAwarded, targetMonth) >= AWARD_COOLDOWN_MONTHS;
    }

    public void markAwarded(YearMonth targetMonth) {
        this.lastAwardedYearMonth = targetMonth.atDay(1);
    }
}
