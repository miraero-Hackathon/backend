package org.jejuro.miraero.domain.autotransfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferStatus;
import org.jejuro.miraero.domain.autotransfer.dto.request.AutoTransferCreateRequest;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AutoTransferServiceImplTest {

  private static final Long USER_ID = 1L;
  private static final Long WITHDRAWAL_ACCOUNT_ID = 10L;
  private static final Long MONEY_BOX_ID = 20L;

  @Mock
  private AutoTransferMapper autoTransferMapper;

  @InjectMocks
  private AutoTransferServiceImpl autoTransferService;

  @Test
  @DisplayName("전달받은 출금계좌와 저금통으로 자동이체 설정을 저장한다")
  void createMoneyBoxAutoTransfer_success() {
    autoTransferService.createMoneyBoxAutoTransfer(
        USER_ID, MONEY_BOX_ID, WITHDRAWAL_ACCOUNT_ID, "123*****90", createRequest()
    );

    ArgumentCaptor<AutoTransfer> captor = ArgumentCaptor.forClass(AutoTransfer.class);
    verify(autoTransferMapper).save(captor.capture());

    AutoTransfer saved = captor.getValue();
    assertEquals(WITHDRAWAL_ACCOUNT_ID, saved.getWithdrawalAccountId());
    assertEquals(MONEY_BOX_ID, saved.getMoneyBoxId());
    assertEquals(300_000L, saved.getTransferAmount());
    assertEquals(5, saved.getTransferDay());
    assertEquals(AutoTransferStatus.ACTIVE, saved.getAutoTransferStatus());
  }

  @Test
  @DisplayName("입금 대상 표시는 저금통 소속 통장의 마스킹 번호를 그대로 저장한다")
  void createMoneyBoxAutoTransfer_usesOwnerAccountMaskedNumber() {
    autoTransferService.createMoneyBoxAutoTransfer(
        USER_ID, MONEY_BOX_ID, WITHDRAWAL_ACCOUNT_ID, "123*****90", createRequest()
    );

    ArgumentCaptor<AutoTransfer> captor = ArgumentCaptor.forClass(AutoTransfer.class);
    verify(autoTransferMapper).save(captor.capture());

    assertEquals("123*****90", captor.getValue().getMaskedDepositAccount());
  }

  private AutoTransferCreateRequest createRequest() {
    AutoTransferCreateRequest request = new AutoTransferCreateRequest();
    ReflectionTestUtils.setField(request, "amount", 300_000L);
    ReflectionTestUtils.setField(request, "transferDay", 5);
    return request;
  }
}
