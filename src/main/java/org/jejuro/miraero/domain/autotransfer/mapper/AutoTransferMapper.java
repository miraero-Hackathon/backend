package org.jejuro.miraero.domain.autotransfer.mapper;


import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferTarget;
import org.jejuro.miraero.domain.goal.domain.AssetType;

@Mapper
public interface AutoTransferMapper {

    void save(AutoTransfer autoTransfer);

    AutoTransfer findByAsset(@Param("assetType")AssetType assetType,
                             @Param("assetId") Long assetId);

    List<AutoTransfer> findByAssets(
            @Param("accountIds") List<Long> accountIds,
            @Param("moneyBoxIds") List<Long> moneyBoxIds
    );

    /**
     * 현재 목표 자동이체 금액
     */
    Long findTargetGoalAutoTransferAmount(@Param("goalId") Long goalId);


    /**
     * 다른 목표 자동이체 금액
     */
    Long findOtherGoalAutoTransferAmount(
            @Param("userId") Long userId,
            @Param("goalId") Long goalId
    );

    // 목표 구분 없이 사용자의 전체 자동이체 금액. 페이스메이커 적립액 계산에 쓴다.
    Long findTotalAutoTransferAmount(@Param("userId") Long userId);



    void deleteByMoneyBoxId(@Param("moneyBoxId") Long moneyBoxId);

    /**
     * 지정한 날짜에 실행할 자동이체 목록을 조회한다.
     *
     * userId가 주어지면 그 사용자 것만 조회한다(시연용 수동 실행).
     */
    List<AutoTransferTarget> findExecutionTargets(
            @Param("executionDate") LocalDate executionDate,
            @Param("userId") Long userId
    );
}
