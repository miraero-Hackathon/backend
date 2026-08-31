package org.jejuro.miraero.domain.autotransfer.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 특정 구간의 저금통 적립 이력 집계.
 *
 * 목표 자동이체와 페이스메이커가 같은 테이블을 쓰므로 둘을 합산한 값이다.
 */
@Getter
@NoArgsConstructor
public class SavingHistorySummary {

    private Long attemptCount;

    private Long successCount;

    /** 페이스메이커 상한에 걸려 일부만 적립된 건 */
    private Long partialCount;

    private Long failedCount;

    private Long totalSavedAmount;

    /** 최장 연속 적립일. 실패한 날은 연속이 끊긴 것으로 본다 */
    private Long maxStreak;
}
