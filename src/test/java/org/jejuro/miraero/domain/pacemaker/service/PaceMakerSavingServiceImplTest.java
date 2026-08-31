package org.jejuro.miraero.domain.pacemaker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.mapper.PaceMakerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaceMakerSavingServiceImplTest {

  private static final LocalDate YESTERDAY = LocalDate.of(2026, 8, 11);
  private static final Long USER_ID = 1L;

  @Mock
  private PaceMakerMapper paceMakerMapper;

  @Mock
  private PaceMakerSaver paceMakerSaver;

  @InjectMocks
  private PaceMakerSavingServiceImpl paceMakerSavingService;

  @Test
  @DisplayName("적립 대상을 모두 처리하고 적립된 건수를 반환한다")
  void saveAll_returnsSavedCount() {
    when(paceMakerMapper.findActiveAutoSavings(null))
        .thenReturn(List.of(autoSaving(1L), autoSaving(2L), autoSaving(3L)));
    when(paceMakerSaver.save(any(), any()))
        .thenReturn(true, false, true);

    assertEquals(2, paceMakerSavingService.saveAll(YESTERDAY, null));

    verify(paceMakerSaver, times(3)).save(any(), any());
  }

  @Test
  @DisplayName("한 건이 예외로 실패해도 나머지는 계속 처리한다")
  void saveAll_oneFails_othersContinue() {
    when(paceMakerMapper.findActiveAutoSavings(null))
        .thenReturn(List.of(autoSaving(1L), autoSaving(2L), autoSaving(3L)));
    when(paceMakerSaver.save(any(), any()))
        .thenReturn(true)
        .thenThrow(new RuntimeException("적립 실패"))
        .thenReturn(true);

    assertEquals(2, paceMakerSavingService.saveAll(YESTERDAY, null));

    verify(paceMakerSaver, times(3)).save(any(), any());
  }

  @Test
  @DisplayName("userId를 넘기면 그 사용자 대상만 조회한다")
  void saveAll_withUserId_filtersTargets() {
    when(paceMakerMapper.findActiveAutoSavings(USER_ID)).thenReturn(List.of());

    assertEquals(0, paceMakerSavingService.saveAll(YESTERDAY, USER_ID));

    verify(paceMakerMapper).findActiveAutoSavings(USER_ID);
  }

  private AutoSaving autoSaving(Long autoSavingId) {
    AutoSaving autoSaving = new AutoSaving();
    ReflectionTestUtils.setField(autoSaving, "autoSavingId", autoSavingId);
    ReflectionTestUtils.setField(autoSaving, "userId", USER_ID);
    ReflectionTestUtils.setField(autoSaving, "moneyBoxId", autoSavingId + 100);
    ReflectionTestUtils.setField(autoSaving, "accountId", 10L);
    ReflectionTestUtils.setField(autoSaving, "autoSavingStatus", "ACTIVE");
    return autoSaving;
  }
}
