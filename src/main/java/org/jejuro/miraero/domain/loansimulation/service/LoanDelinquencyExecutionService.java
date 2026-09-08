package org.jejuro.miraero.domain.loansimulation.service;

import java.time.LocalDate;

public interface LoanDelinquencyExecutionService {

    /**
     * 대출 시뮬레이션(연체/연속상환) 판정을 실행한다.
     *
     * @param executionDate 판정 기준일
     * @param userId 특정 유저만 실행할 때 지정. null이면 전체 유저 대상
     * @return 실제로 처리한 목표 건수
     */
    int executeAll(LocalDate executionDate, Long userId);

}
