package org.jejuro.miraero.domain.autotransfer.service;

public interface AutoTransferQueryService {
    /**
     * 목표에 설정된 자동이체 금액 합계를 조회한다.
     *
     * @param goalId 목표 ID
     * @return 목표 자동이체 금액 합계
     */
    Long getTargetGoalTransferAmount(
            Long goalId
    );

    /**
     * 사용자의 다른 목표에 설정된 자동이체 금액 합계를 조회한다.
     *
     * @param userId 사용자 ID
     * @param goalId 제외할 목표 ID
     * @return 다른 목표 자동이체 금액 합계
     */
    Long getOtherGoalTransferAmount(
            Long userId,
            Long goalId
    );

    /**
     * 사용자의 전체 자동이체 금액 합계를 조회한다.
     *
     * 특정 목표를 기준으로 나누지 않으므로 페이스메이커처럼 목표와 무관한 계산에 쓴다.
     *
     * @param userId 사용자 ID
     * @return 전체 자동이체 금액 합계
     */
    Long getTotalTransferAmount(Long userId);
}
