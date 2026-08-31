package org.jejuro.miraero.domain.autotransfer.service;

import java.time.LocalDate;

public interface AutoTransferExecutionService {

    /**
     * 해당 날짜에 실행할 저금통 자동이체를 모두 처리한다.
     *
     * @param executionDate 실행 기준일
     * @param userId 특정 사용자만 실행할 때 지정. null이면 전체
     * @return 실제로 적립된 건수
     */
    int executeAll(LocalDate executionDate, Long userId);
}
