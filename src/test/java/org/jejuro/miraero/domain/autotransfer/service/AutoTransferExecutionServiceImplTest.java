package org.jejuro.miraero.domain.autotransfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferTarget;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoTransferExecutionServiceImplTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 8, 12);
  private static final Long USER_ID = 1L;

  @Mock
  private AutoTransferMapper autoTransferMapper;

  @Mock
  private AutoTransferExecutor autoTransferExecutor;

  @InjectMocks
  private AutoTransferExecutionServiceImpl autoTransferExecutionService;

  @Test
  @DisplayName("실행 대상을 모두 처리하고 적립된 건수를 반환한다")
  void executeAll_returnsExecutedCount() {
    when(autoTransferMapper.findExecutionTargets(TODAY, null))
        .thenReturn(List.of(target(1L), target(2L), target(3L)));
    when(autoTransferExecutor.execute(any(), any()))
        .thenReturn(true, false, true);

    assertEquals(2, autoTransferExecutionService.executeAll(TODAY, null));

    verify(autoTransferExecutor, times(3)).execute(any(), any());
  }

  @Test
  @DisplayName("한 건이 예외로 실패해도 나머지는 계속 처리한다")
  void executeAll_oneFails_othersContinue() {
    when(autoTransferMapper.findExecutionTargets(TODAY, null))
        .thenReturn(List.of(target(1L), target(2L), target(3L)));
    when(autoTransferExecutor.execute(any(), any()))
        .thenReturn(true)
        .thenThrow(new RuntimeException("적립 실패"))
        .thenReturn(true);

    assertEquals(2, autoTransferExecutionService.executeAll(TODAY, null));

    verify(autoTransferExecutor, times(3)).execute(any(), any());
  }

  @Test
  @DisplayName("userId를 넘기면 그 사용자 대상만 조회한다")
  void executeAll_withUserId_filtersTargets() {
    when(autoTransferMapper.findExecutionTargets(TODAY, USER_ID))
        .thenReturn(List.of());

    assertEquals(0, autoTransferExecutionService.executeAll(TODAY, USER_ID));

    verify(autoTransferMapper).findExecutionTargets(TODAY, USER_ID);
  }

  @Test
  @DisplayName("실행 대상이 없으면 아무것도 하지 않는다")
  void executeAll_noTargets_doesNothing() {
    when(autoTransferMapper.findExecutionTargets(TODAY, null))
        .thenReturn(List.of());

    assertEquals(0, autoTransferExecutionService.executeAll(TODAY, null));
  }

  private AutoTransferTarget target(Long autoTransferId) {
    return AutoTransferTarget.builder()
        .autoTransferId(autoTransferId)
        .moneyBoxId(autoTransferId + 100)
        .accountId(10L)
        .userId(USER_ID)
        .transferAmount(300_000L)
        .availableBalance(2_000_000L)
        .build();
  }
}
