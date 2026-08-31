package org.jejuro.miraero.domain.pacemaker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.moneybox.service.MoneyBoxService;
import org.jejuro.miraero.domain.mydata.service.AccountTransferService;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerCreateRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerGoalDepositRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerHistorySearchCondition;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerCreateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDepositAssetResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositAssetRowResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalListResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalWithdrawalAccountRowResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerHistoryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMaxAmountUpdateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMoneyBoxResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerTodaySavingResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWeeklyStreakResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWithdrawalAccountResponse;
import org.jejuro.miraero.domain.pacemaker.exception.PaceMakerErrorCode;
import org.jejuro.miraero.domain.pacemaker.mapper.PaceMakerMapper;
import org.jejuro.miraero.domain.transaction.service.TransactionQueryService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaceMakerServiceImpl implements PaceMakerService {

  private final PaceMakerMapper paceMakerMapper;
  private final MoneyBoxMapper moneyBoxMapper;
  private final AccountMapper accountMapper;
  private final MoneyBoxService moneyBoxService;
  private final TransactionQueryService transactionQueryService;
  private final AccountTransferService accountTransferService;

  @Override
  @Transactional
  public PaceMakerCreateResponse createPaceMaker(Long userId, PaceMakerCreateRequest request) {

    // 사용자당 1개(uk_auto_saving_user). 먼저 걸러 DB 예외 대신 409로 응답한다.
    if (paceMakerMapper.findByUserId(userId) != null) {
      throw new BusinessException(PaceMakerErrorCode.ALREADY_REGISTERED);
    }

    Long accountId = resolveAccountId(userId, request.getAccountId());

    // 매일 여유자금을 적립하므로 급여가 들어오는 통장이 아니면 잔액이 고갈된다
    moneyBoxService.validateAutoWithdrawalAccount(userId, accountId);

    Long moneyBoxId = moneyBoxService.createMoneyBox(userId, accountId, MoneyBoxType.SAVING);

    // 서브 레저라 출금과 입금이 같은 계좌 안에서 일어난다
    paceMakerMapper.insertAutoSaving(userId, moneyBoxId, accountId, request.getMaxAmount());

    return PaceMakerCreateResponse.from(paceMakerMapper.findByUserId(userId));
  }

  /**
   * 어차피 급여 통장에만 만들 수 있어서, 화면이 계좌를 묻지 않으면 서버가 찾아 쓴다.
   */
  private Long resolveAccountId(Long userId, Long requestedAccountId) {
    if (requestedAccountId != null) {
      return requestedAccountId;
    }

    Long salaryAccountId = transactionQueryService.getSalaryAccountId(userId);

    if (salaryAccountId == null) {
      throw new BusinessException(PaceMakerErrorCode.SALARY_ACCOUNT_NOT_FOUND);
    }

    return salaryAccountId;
  }

  @Override
  public PaceMakerResponse getPaceMaker(Long userId) {
    AutoSaving autoSaving = paceMakerMapper.findByUserId(userId);
    return PaceMakerResponse.from(autoSaving);
  }

  @Override
  @Transactional
  public PaceMakerResponse updateStatus(Long userId, Long autoSavingId, String status) {
    int updatedCount = paceMakerMapper.updateStatus(userId, autoSavingId, status);

    if (updatedCount == 0) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    AutoSaving autoSaving = paceMakerMapper.findByUserId(userId);
    return PaceMakerResponse.from(autoSaving);
  }

  @Override
  @Transactional(readOnly = true)
  public PaceMakerDashboardResponse getDashboard(Long userId, boolean includeStreak) {
    PaceMakerDashboardSummaryResponse summary = paceMakerMapper.findDashboardByUserId(userId);

    if (summary == null) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    Integer currentStreak = null;
    List<PaceMakerWeeklyStreakResponse> weeklyStreak = null;
    Integer monthlySuccessCount = null;

    if (includeStreak) {
      Long autoSavingId = summary.getAutoSavingId();

      currentStreak = calculateCurrentStreak(
          paceMakerMapper.findRecentSavingDates(autoSavingId)
      );
      weeklyStreak = paceMakerMapper.findWeeklyStreak(autoSavingId);
      monthlySuccessCount = paceMakerMapper.countMonthlySuccess(autoSavingId);
    }

    return PaceMakerDashboardResponse.builder()
        .autoSavingId(summary.getAutoSavingId())
        .status(summary.getStatus())
        .maxAmount(summary.getMaxAmount())
        .moneyBox(PaceMakerMoneyBoxResponse.builder()
            .moneyBoxId(summary.getMoneyBoxId())
            .balance(summary.getBalance())
            .maskedAccountNumber(summary.getMaskedAccountNumber())
            .build())
        .todaySaving(PaceMakerTodaySavingResponse.builder()
            .saved(Boolean.TRUE.equals(summary.getTodaySaved()))
            .amount(summary.getTodaySavingAmount())
            .build())
        .currentStreak(currentStreak)
        .weeklyStreak(weeklyStreak)
        .monthlySuccessCount(monthlySuccessCount)
        .build();
  }

  @Override
  @Transactional
  public PaceMakerMaxAmountUpdateResponse updateMaxAmount(Long userId, Long autoSavingId,
      Long maxAmount) {
    int updateCount = paceMakerMapper.updateMaxAmount(userId, autoSavingId, maxAmount);

    if (updateCount == 0) {
      throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    return PaceMakerMaxAmountUpdateResponse.builder()
        .autoSavingId(autoSavingId)
        .maxAmount(maxAmount)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<PaceMakerHistoryResponse> getHistories(
      Long userId,
      PaceMakerHistorySearchCondition condition
  ) {
    condition.validate();

    LocalDateTime startDateTime = condition.resolveYearMonth().atDay(1).atStartOfDay();

    LocalDateTime endDateTime = startDateTime.plusMonths(1);

    List<PaceMakerHistoryResponse> histories = paceMakerMapper.findHistories(
        userId,
        startDateTime,
        endDateTime,
        condition.getOffset(),
        condition.getSize()
    );

    long totalElements = paceMakerMapper.countHistories(
        userId,
        startDateTime,
        endDateTime
    );

    return PageResponse.of(
        histories,
        condition.getPage(),
        condition.getSize(),
        totalElements
    );
  }

  private int calculateCurrentStreak(List<LocalDate> savingDates) {
    if (savingDates == null || savingDates.isEmpty()) {
      return 0;
    }

    LocalDate expectedDate = LocalDate.now();

    if (!savingDates.get(0).equals(expectedDate)) {
      expectedDate = expectedDate.minusDays(1);
    }

    int streak = 0;

    for (LocalDate savingDate : savingDates) {
      if (!savingDate.equals(expectedDate)) {
        break;
      }

      streak++;
      expectedDate = expectedDate.minusDays(1);
    }

    return streak;
  }

  @Override
  @Transactional(readOnly = true)
  public PaceMakerGoalListResponse getPaceMakerGoals(Long userId) {
    AutoSaving autoSaving = paceMakerMapper.findByUserId(userId);

    if (autoSaving == null) {
      throw new BusinessException(PaceMakerErrorCode.NOT_REGISTERED);
    }

    //목표, 입금, 출금계좌 조회
    List<PaceMakerGoalSummaryResponse> goals = paceMakerMapper.findPaceMakerGoals(userId);
    List<PaceMakerGoalDepositAssetRowResponse> depositAssetRows =
        paceMakerMapper.findPaceMakerGoalDepositAssets(userId);
    List<PaceMakerGoalWithdrawalAccountRowResponse> withdrawalAccountRows =
        paceMakerMapper.findPaceMakerGoalWithdrawalAccounts(userId);

    //goalId 기준으로 응답을 조립
    Map<Long, List<PaceMakerGoalDepositAssetRowResponse>> depositAssetsByGoalId =
        depositAssetRows.stream()
            .collect(Collectors.groupingBy(PaceMakerGoalDepositAssetRowResponse::getGoalId));

    Map<Long, List<PaceMakerGoalWithdrawalAccountRowResponse>> withdrawalAccountsByGoalId =
        withdrawalAccountRows.stream()
            .collect(Collectors.groupingBy(PaceMakerGoalWithdrawalAccountRowResponse::getGoalId));

    //최종 응답 조립
    List<PaceMakerGoalResponse> goalResponses = goals.stream()
        .map(goal -> {
          List<PaceMakerDepositAssetResponse> depositAssets =
              depositAssetsByGoalId
                  .getOrDefault(goal.getGoalId(), Collections.emptyList())
                  .stream()
                  .map(asset -> PaceMakerDepositAssetResponse.builder()
                      .assetType(asset.getAssetType())
                      .assetId(asset.getAssetId())
                      .financialInstitutionName(asset.getFinancialInstitutionName())
                      .maskedAccountNumber(asset.getMaskedAccountNumber())
                      .balance(asset.getBalance())
                      .build())
                  .collect(Collectors.toList());

          Long totalSavedAmount = depositAssets.stream()
              .map(PaceMakerDepositAssetResponse::getBalance)
              .filter(balance -> balance != null)
              .reduce(0L, Long::sum);

          List<PaceMakerWithdrawalAccountResponse> withdrawalAccounts =
              withdrawalAccountsByGoalId
                  .getOrDefault(goal.getGoalId(), Collections.emptyList())
                  .stream()
                  .map(account -> PaceMakerWithdrawalAccountResponse.builder()
                      .accountId(account.getAccountId())
                      .financialInstitutionName(account.getFinancialInstitutionName())
                      .maskedAccountNumber(account.getMaskedAccountNumber())
                      .balance(account.getBalance())
                      .build())
                  .collect(Collectors.toList());

          return PaceMakerGoalResponse.builder()
              .goalId(goal.getGoalId())
              .goalName(goal.getGoalName())
              .goalType(goal.getGoalType())
              .goalAmount(goal.getGoalAmount())
              .totalSavedAmount(totalSavedAmount)
              .depositAssets(depositAssets)
              .withdrawalAccounts(withdrawalAccounts)
              .build();
        })
        .collect(Collectors.toList());

    return PaceMakerGoalListResponse.builder()
        .goals(goalResponses)
        .build();
  }

  @Override
  @Transactional
  public PaceMakerGoalDepositResponse depositToGoal(Long userId,
      PaceMakerGoalDepositRequest request) {
    if (request.getAmount() == null || request.getAmount() <= 0) {
      throw new BusinessException(PaceMakerErrorCode.INVALID_DEPOSIT_AMOUNT);
    }

    // 목표 자산에는 LOAN도 있을 수 있지만 저축금을 넣을 대상이 아니다
    if (request.getAssetType() != AssetType.ACCOUNT
        && request.getAssetType() != AssetType.MONEY_BOX) {
      throw new BusinessException(PaceMakerErrorCode.FORBIDDEN_GOAL_ACCOUNT);
    }

    MoneyBox savingMoneyBox = moneyBoxMapper.findPaceMakerMoneyBoxByIdAndUserIdForUpdate(
        request.getMoneyBoxId(),
        userId
    );

    if (savingMoneyBox == null) {
      throw new BusinessException(PaceMakerErrorCode.NOT_REGISTERED);
    }

    boolean existsGoalDepositAsset =
        paceMakerMapper.existsGoalDepositAssetByUserIdAndAsset(
            userId,
            request.getAssetType(),
            request.getAssetId()
        );

    if (!existsGoalDepositAsset) {
      throw new BusinessException(PaceMakerErrorCode.FORBIDDEN_GOAL_ACCOUNT);
    }

    Long balance = savingMoneyBox.getBalance();

    if (balance < request.getAmount()) {
      throw new BusinessException(PaceMakerErrorCode.INSUFFICIENT_BALANCE);
    }

    moneyBoxMapper.decreaseBalance(
        savingMoneyBox.getMoneyBoxId(),
        userId,
        request.getAmount()
    );

    if (request.getAssetType() == AssetType.MONEY_BOX) {
      depositToMoneyBox(userId, savingMoneyBox, request.getAssetId(), request.getAmount());
    } else {
      depositToAccount(userId, savingMoneyBox, request.getAssetId(), request.getAmount());
    }

    return PaceMakerGoalDepositResponse.builder()
        .assetType(request.getAssetType())
        .assetId(request.getAssetId())
        .depositedAmount(request.getAmount())
        .remainingBalance(balance - request.getAmount())
        .build();
  }

  private void depositToAccount(
      Long userId, MoneyBox savingMoneyBox, Long targetAccountId, Long amount) {
    // 저금통이 속한 계좌와 목표 계좌는 항상 다른 물리 계좌라 실제 이체가 필요하다.
    // 로컬 잔액만 올리면 다음 마이데이터 동기화 때 mock 서버의 옛 값으로 덮어써져 돈이 증발한다.
    accountTransferService.transfer(
        userId, savingMoneyBox.getAccountId(), targetAccountId, amount);
    accountMapper.increaseBalance(targetAccountId, userId, amount);
  }

  private void depositToMoneyBox(
      Long userId, MoneyBox savingMoneyBox, Long targetMoneyBoxId, Long amount) {
    MoneyBox targetMoneyBox = moneyBoxMapper.findById(targetMoneyBoxId);

    if (targetMoneyBox == null) {
      throw new BusinessException(PaceMakerErrorCode.FORBIDDEN_GOAL_ACCOUNT);
    }

    // 두 저금통이 같은 계좌 소속이면 계좌를 나가는 돈이 없다. 서브 레저 안에서
    // 몫만 옮기면 되므로 실이체를 거치지 않는다.
    if (!targetMoneyBox.getAccountId().equals(savingMoneyBox.getAccountId())) {
      accountTransferService.transfer(
          userId, savingMoneyBox.getAccountId(), targetMoneyBox.getAccountId(), amount);
      accountMapper.increaseBalance(targetMoneyBox.getAccountId(), userId, amount);
    }

    moneyBoxMapper.increaseBalance(targetMoneyBoxId, amount);
  }
}
