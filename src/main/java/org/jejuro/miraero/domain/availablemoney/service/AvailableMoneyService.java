package org.jejuro.miraero.domain.availablemoney.service;

import java.time.LocalDate;

import org.jejuro.miraero.domain.availablemoney.dto.response.DailyAvailableMoneyResponse;
import org.jejuro.miraero.domain.availablemoney.dto.response.MonthlyAvailableMoneyResponse;

public interface AvailableMoneyService {
    /**
     * 목표에 대한 월간 여유자금을 조회한다.
     *
     * @param userId 사용자 ID
     * @param goalId 여유자금을 계산할 목표 ID
     * @return 월간 여유자금 조회 결과
     */
    MonthlyAvailableMoneyResponse getMonthlyAvailableMoney(Long userId, Long goalId);


    /**
     * 목표에 대한 일간 여유자금을 조회한다.
     *
     * @param userId 사용자 ID
     * @param goalId 여유자금을 계산할 목표 ID
     * @return 일간 여유자금 조회 결과
     */
    DailyAvailableMoneyResponse getDailyAvailableMoney(Long userId, Long goalId);

    /**
     * 기준일에 쓰고 남은 여유자금을 조회한다.
     *
     * 조회용 메서드와 달리 기준일을 받고 목표를 구분하지 않는다.
     * 페이스메이커가 매일 08:00에 전날 구간을 정산할 때 쓴다.
     *
     * @param userId 사용자 ID
     * @param businessDate 정산할 영업일 (해당일 08:00 ~ 다음날 08:00 구간)
     * @return 그날 쓰고 남은 금액. 예산을 초과했으면 음수
     */
    Long getRemainingMoneyOf(Long userId, LocalDate businessDate);
}
