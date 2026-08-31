package org.jejuro.miraero.domain.autotransfer.mapper;

import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.autotransfer.domain.SavingHistorySummary;
import org.jejuro.miraero.domain.autotransfer.domain.TransferStatus;

/**
 * 저금통 적립 이력. 페이스메이커와 목표 자동이체가 같은 테이블을 쓴다.
 */
@Mapper
public interface SavingHistoryMapper {

  /**
   * (money_box_id, transacted_at) UNIQUE 제약이 하루 한 건을 보장하므로,
   * 이미 기록이 있으면 0을 반환하고 아무것도 쓰지 않는다.
   *
   * @return 실제로 기록된 행 수. 이미 실행된 건이면 0
   */
  int insertIgnoreDuplicate(
      @Param("moneyBoxId") Long moneyBoxId,
      @Param("amount") Long amount,
      @Param("transactedAt") LocalDate transactedAt,
      @Param("transferStatus") TransferStatus transferStatus
  );

  /**
   * 페이스메이커 적립 이력. 대시보드·연속적립일 조회가 auto_saving_id로 필터하므로
   * 목표 자동이체와 달리 이 값을 반드시 채운다.
   *
   * @return 실제로 기록된 행 수. 이미 실행된 건이면 0
   */
  int insertPaceMakerIgnoreDuplicate(
      @Param("moneyBoxId") Long moneyBoxId,
      @Param("autoSavingId") Long autoSavingId,
      @Param("amount") Long amount,
      @Param("transactedAt") LocalDate transactedAt,
      @Param("transferStatus") TransferStatus transferStatus
  );

  /**
   * 구간 내 적립 이력을 집계한다. 마일스톤 리포트가 적립 성실도를 판단하는 데 쓴다.
   *
   * @return 이력이 하나도 없어도 0으로 채워진 객체를 반환한다
   */
  SavingHistorySummary findSummary(
      @Param("userId") Long userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );
}
