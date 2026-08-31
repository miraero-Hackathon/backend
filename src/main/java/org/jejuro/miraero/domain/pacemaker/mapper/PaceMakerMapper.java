package org.jejuro.miraero.domain.pacemaker.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositAssetRowResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalWithdrawalAccountRowResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerHistoryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWeeklyStreakResponse;

@Mapper
public interface PaceMakerMapper {

  AutoSaving findByUserId(@Param("userId") Long userId);

  int insertAutoSaving(
      @Param("userId") Long userId,
      @Param("moneyBoxId") Long moneyBoxId,
      @Param("accountId") Long accountId,
      @Param("maxAmount") Long maxAmount
  );

  /**
   * 적립 대상인 페이스메이커 설정 목록.
   *
   * userId가 주어지면 그 사용자 것만 조회한다(시연용 수동 실행).
   */
  List<AutoSaving> findActiveAutoSavings(@Param("userId") Long userId);

  int updateStatus(
      @Param("userId") Long userId,
      @Param("autoSavingId") Long autoSavingId,
      @Param("status") String status);

  int updateMaxAmount(
      @Param("userId") Long userId,
      @Param("autoSavingId") Long autoSavingId,
      @Param("maxAmount") Long maxAmount
  );

  PaceMakerDashboardSummaryResponse findDashboardByUserId(@Param("userId") Long userId);

  Integer countMonthlySuccess(@Param("autoSavingId") Long autoSavingId);

  List<PaceMakerWeeklyStreakResponse> findWeeklyStreak(@Param("autoSavingId") Long autoSavingId);

  List<LocalDate> findRecentSavingDates(@Param("autoSavingId") Long autoSavingId);

  List<PaceMakerHistoryResponse> findHistories(
      @Param("userId") Long userId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime,
      @Param("offset") Long offset,
      @Param("size") Integer size
  );

  long countHistories(
      @Param("userId") Long userId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime
  );

  List<PaceMakerGoalSummaryResponse> findPaceMakerGoals(@Param("userId") Long userId);

  List<PaceMakerGoalDepositAssetRowResponse> findPaceMakerGoalDepositAssets(
      @Param("userId") Long userId
  );

  List<PaceMakerGoalWithdrawalAccountRowResponse> findPaceMakerGoalWithdrawalAccounts(
      @Param("userId") Long userId
  );

  boolean existsGoalDepositAssetByUserIdAndAsset(
      @Param("userId") Long userId,
      @Param("assetType") AssetType assetType,
      @Param("assetId") Long assetId
  );
}
