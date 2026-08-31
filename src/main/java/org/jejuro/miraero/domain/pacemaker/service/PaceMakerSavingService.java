package org.jejuro.miraero.domain.pacemaker.service;

import java.time.LocalDate;

public interface PaceMakerSavingService {

    /**
     * 기준일에 쓰고 남은 여유자금을 페이스메이커 저금통에 적립한다.
     *
     * @param businessDate 정산할 영업일 (해당일 08:00 ~ 다음날 08:00 구간)
     * @param userId 특정 사용자만 실행할 때 지정. null이면 전체
     * @return 실제로 적립된 건수
     */
    int saveAll(LocalDate businessDate, Long userId);
}
