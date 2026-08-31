package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalPullFundsResponse;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetListResponse;

import java.util.List;
import java.util.Map;

public interface GoalAssetService {

    void saveGoalAssets(Long userId, Long goalId, List<GoalAssetRequest> assets);

    /**
     * 여유자금 부족으로 밀린 목표에 다른 입출금 계좌의 돈을 끌어다 채운다.
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @param request 출처 계좌와 금액
     */
    GoalPullFundsResponse pullFunds(Long userId, Long goalId, GoalPullFundsRequest request);

    /**
     * 목표에 연결된 자산의 현재 금액을 계산한다
     *
     * @param goalId 목표 ID
     * @return 목표에 연결된 자산의 현재 금액 합산
     */
    Long calculateCurrentAmount(Long userId, Long goalId);

    Map<Long, Long> calculateCurrentAmounts(
            Long userId,
            List<Long> goalIds
    );

    GoalAssetListResponse getGoalAssets(Long userId, Long goalId);

    /**
     * 목표 자산 연결 해제
     *
     * 목표와 연결된 자산을 제거한다.
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     * @param assetType 자산 타입
     * @param assetId 자산 ID
     */
    void deleteGoalAsset(Long userId, Long goalId, AssetType assetType, Long assetId);

    /**
     * 목표 생성 시 사용자가 이미 담아둔 금액을 연결된 저금통 잔액으로 반영한다.
     *
     * @param goalId 목표 ID
     * @param startAmount 목표 시작 금액
     */
    void applyStartAmount(Long goalId, Long startAmount);

    /**
     * 목표 삭제 시 연결된 저금통과 자동이체를 정리한다.
     *
     * 저금통이 사라지면 묶여 있던 금액이 소속 계좌 잔액으로 돌아온다.
     *
     * @param goalId 목표 ID
     */
    void releaseGoalAssets(Long goalId);
}
