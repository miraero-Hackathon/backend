package org.jejuro.miraero.domain.pacemaker.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.domain.TransferStatus;
import org.jejuro.miraero.domain.autotransfer.mapper.SavingHistoryMapper;
import org.jejuro.miraero.domain.availablemoney.service.AvailableMoneyService;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaceMakerSaverTest {

  private static final LocalDate YESTERDAY = LocalDate.of(2026, 8, 11);
  private static final Long USER_ID = 1L;
  private static final Long AUTO_SAVING_ID = 3L;
  private static final Long MONEY_BOX_ID = 5L;
  private static final Long ACCOUNT_ID = 10L;

  @Mock
  private AvailableMoneyService availableMoneyService;

  @Mock
  private AccountMapper accountMapper;

  @Mock
  private SavingHistoryMapper savingHistoryMapper;

  @Mock
  private MoneyBoxMapper moneyBoxMapper;

  @InjectMocks
  private PaceMakerSaver paceMakerSaver;

  @Test
  @DisplayName("어제 남은 여유자금만큼 저금통에 적립한다")
  void save_remainingMoney_deposits() {
    givenRemaining(12_000L);
    givenAccountBalance(2_000_000L);
    when(savingHistoryMapper.insertPaceMakerIgnoreDuplicate(
        eq(MONEY_BOX_ID), eq(AUTO_SAVING_ID), eq(12_000L), eq(YESTERDAY),
        eq(TransferStatus.SUCCESS)))
        .thenReturn(1);

    assertTrue(paceMakerSaver.save(autoSaving(null), YESTERDAY));

    verify(moneyBoxMapper).increaseBalance(MONEY_BOX_ID, 12_000L);
  }

  @Test
  @DisplayName("남은 여유자금이 상한을 넘으면 상한까지만 적립하고 PARTIAL_LIMIT으로 기록한다")
  void save_exceedsMaxAmount_capsAtLimit() {
    givenRemaining(50_000L);
    givenAccountBalance(2_000_000L);
    when(savingHistoryMapper.insertPaceMakerIgnoreDuplicate(
        eq(MONEY_BOX_ID), eq(AUTO_SAVING_ID), eq(30_000L), eq(YESTERDAY),
        eq(TransferStatus.PARTIAL_LIMIT)))
        .thenReturn(1);

    assertTrue(paceMakerSaver.save(autoSaving(30_000L), YESTERDAY));

    verify(moneyBoxMapper).increaseBalance(MONEY_BOX_ID, 30_000L);
  }

  @Test
  @DisplayName("상한이 없으면 남은 금액을 그대로 적립한다")
  void save_noMaxAmount_savesAll() {
    givenRemaining(50_000L);
    givenAccountBalance(2_000_000L);
    when(savingHistoryMapper.insertPaceMakerIgnoreDuplicate(
        any(), any(), eq(50_000L), any(), eq(TransferStatus.SUCCESS)))
        .thenReturn(1);

    assertTrue(paceMakerSaver.save(autoSaving(null), YESTERDAY));

    verify(moneyBoxMapper).increaseBalance(MONEY_BOX_ID, 50_000L);
  }

  @Test
  @DisplayName("예산을 넘겨 써서 남은 돈이 없으면 적립하지 않고 이력도 남기지 않는다")
  void save_noRemaining_skips() {
    givenRemaining(-5_000L);

    assertFalse(paceMakerSaver.save(autoSaving(null), YESTERDAY));

    verify(savingHistoryMapper, never())
        .insertPaceMakerIgnoreDuplicate(any(), any(), any(), any(), any());
    verify(moneyBoxMapper, never()).increaseBalance(anyLong(), anyLong());
  }

  @Test
  @DisplayName("남은 돈이 정확히 0이면 적립하지 않는다")
  void save_zeroRemaining_skips() {
    givenRemaining(0L);

    assertFalse(paceMakerSaver.save(autoSaving(null), YESTERDAY));

    verify(moneyBoxMapper, never()).increaseBalance(anyLong(), anyLong());
  }

  @Test
  @DisplayName("통장 잔액이 부족하면 적립하지 않고 실패로 기록한다")
  void save_insufficientAccountBalance_recordsFailure() {
    givenRemaining(50_000L);
    givenAccountBalance(10_000L);

    assertFalse(paceMakerSaver.save(autoSaving(null), YESTERDAY));

    verify(savingHistoryMapper).insertPaceMakerIgnoreDuplicate(
        MONEY_BOX_ID, AUTO_SAVING_ID, 0L, YESTERDAY,
        TransferStatus.FAILED_INSUFFICIENT_FUNDS);
    verify(moneyBoxMapper, never()).increaseBalance(anyLong(), anyLong());
  }

  @Test
  @DisplayName("이미 적립된 날짜면 다시 적립하지 않는다")
  void save_alreadySaved_skips() {
    givenRemaining(12_000L);
    givenAccountBalance(2_000_000L);
    // UNIQUE 제약에 걸려 INSERT IGNORE가 0을 반환하는 상황
    when(savingHistoryMapper.insertPaceMakerIgnoreDuplicate(
        any(), any(), any(), any(), any()))
        .thenReturn(0);

    assertFalse(paceMakerSaver.save(autoSaving(null), YESTERDAY));

    verify(moneyBoxMapper, never()).increaseBalance(anyLong(), anyLong());
  }

  private void givenRemaining(long amount) {
    when(availableMoneyService.getRemainingMoneyOf(USER_ID, YESTERDAY)).thenReturn(amount);
  }

  private void givenAccountBalance(long balance) {
    when(accountMapper.findResponseById(ACCOUNT_ID))
        .thenReturn(AccountResponse.builder().accountId(ACCOUNT_ID).balance(balance).build());
  }

  private AutoSaving autoSaving(Long maxAmount) {
    AutoSaving autoSaving = new AutoSaving();
    ReflectionTestUtils.setField(autoSaving, "autoSavingId", AUTO_SAVING_ID);
    ReflectionTestUtils.setField(autoSaving, "userId", USER_ID);
    ReflectionTestUtils.setField(autoSaving, "moneyBoxId", MONEY_BOX_ID);
    ReflectionTestUtils.setField(autoSaving, "accountId", ACCOUNT_ID);
    ReflectionTestUtils.setField(autoSaving, "maxAmount", maxAmount);
    ReflectionTestUtils.setField(autoSaving, "autoSavingStatus", "ACTIVE");
    return autoSaving;
  }
}
