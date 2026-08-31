package org.jejuro.miraero.domain.autotransfer.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferTarget;
import org.jejuro.miraero.domain.autotransfer.domain.TransferStatus;
import org.jejuro.miraero.domain.autotransfer.mapper.SavingHistoryMapper;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoTransferExecutorTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 8, 12);
  private static final Long MONEY_BOX_ID = 5L;

  @Mock
  private SavingHistoryMapper savingHistoryMapper;

  @Mock
  private MoneyBoxMapper moneyBoxMapper;

  @InjectMocks
  private AutoTransferExecutor autoTransferExecutor;

  @Test
  @DisplayName("잔액이 충분하면 저금통 잔액을 늘리고 SUCCESS로 기록한다")
  void execute_enoughBalance_increasesMoneyBox() {
    when(savingHistoryMapper.insertIgnoreDuplicate(
        eq(MONEY_BOX_ID), eq(300_000L), eq(TODAY), eq(TransferStatus.SUCCESS)))
        .thenReturn(1);

    boolean result = autoTransferExecutor.execute(target(300_000L, 2_000_000L), TODAY);

    assertTrue(result);
    verify(moneyBoxMapper).increaseBalance(MONEY_BOX_ID, 300_000L);
  }

  @Test
  @DisplayName("계좌 잔액은 건드리지 않는다 (서브 레저이므로 earmark만 늘린다)")
  void execute_doesNotTouchAccountBalance() {
    when(savingHistoryMapper.insertIgnoreDuplicate(any(), any(), any(), any()))
        .thenReturn(1);

    autoTransferExecutor.execute(target(300_000L, 2_000_000L), TODAY);

    // 계좌 잔액을 바꾸는 호출이 전혀 없어야 한다
    verify(moneyBoxMapper).increaseBalance(anyLong(), anyLong());
  }

  @Test
  @DisplayName("가용 잔액이 이체 금액보다 적으면 적립하지 않고 실패로 기록한다")
  void execute_insufficientBalance_recordsFailure() {
    when(savingHistoryMapper.insertIgnoreDuplicate(
        eq(MONEY_BOX_ID), eq(0L), eq(TODAY), eq(TransferStatus.FAILED_INSUFFICIENT_FUNDS)))
        .thenReturn(1);

    boolean result = autoTransferExecutor.execute(target(300_000L, 120_000L), TODAY);

    assertFalse(result);
    verify(moneyBoxMapper, never()).increaseBalance(anyLong(), anyLong());
  }

  @Test
  @DisplayName("가용 잔액이 이체 금액과 같으면 적립한다")
  void execute_exactBalance_succeeds() {
    when(savingHistoryMapper.insertIgnoreDuplicate(any(), any(), any(), any()))
        .thenReturn(1);

    assertTrue(autoTransferExecutor.execute(target(300_000L, 300_000L), TODAY));

    verify(moneyBoxMapper).increaseBalance(MONEY_BOX_ID, 300_000L);
  }

  @Test
  @DisplayName("오늘 이미 실행된 건이면 다시 적립하지 않는다")
  void execute_alreadyExecuted_skips() {
    // UNIQUE 제약에 걸려 INSERT IGNORE가 0을 반환하는 상황
    when(savingHistoryMapper.insertIgnoreDuplicate(any(), any(), any(), any()))
        .thenReturn(0);

    boolean result = autoTransferExecutor.execute(target(300_000L, 2_000_000L), TODAY);

    assertFalse(result);
    verify(moneyBoxMapper, never()).increaseBalance(anyLong(), anyLong());
  }

  private AutoTransferTarget target(Long transferAmount, Long availableBalance) {
    return AutoTransferTarget.builder()
        .autoTransferId(1L)
        .moneyBoxId(MONEY_BOX_ID)
        .accountId(10L)
        .userId(1L)
        .transferAmount(transferAmount)
        .availableBalance(availableBalance)
        .build();
  }
}
