package org.jejuro.miraero.domain.moneybox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.dto.request.AutoTransferCreateRequest;
import org.jejuro.miraero.domain.autotransfer.service.AutoTransferService;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;
import org.jejuro.miraero.domain.moneybox.dto.request.MoneyBoxCreateRequest;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.transaction.service.TransactionQueryService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MoneyBoxServiceImplTest {

  private static final Long USER_ID = 1L;
  private static final Long CHECKING_ACCOUNT_ID = 10L;
  private static final Long SAVINGS_ACCOUNT_ID = 11L;

  @Mock
  private MoneyBoxMapper moneyBoxMapper;

  @Mock
  private AccountMapper accountMapper;

  @Mock
  private AutoTransferService autoTransferService;

  @Mock
  private TransactionQueryService transactionQueryService;

  @InjectMocks
  private MoneyBoxServiceImpl moneyBoxService;

  @Test
  @DisplayName("선택한 입출금통장을 소속 계좌로 저금통을 만든다")
  void createMoneyBox_setsOwnerAccount() {
    when(accountMapper.findByIdAndUserId(CHECKING_ACCOUNT_ID, USER_ID))
        .thenReturn(account(CHECKING_ACCOUNT_ID, "CHECKING"));

    moneyBoxService.createMoneyBox(USER_ID, request(CHECKING_ACCOUNT_ID, null));

    ArgumentCaptor<MoneyBox> captor = ArgumentCaptor.forClass(MoneyBox.class);
    verify(moneyBoxMapper).insert(captor.capture());

    MoneyBox saved = captor.getValue();
    assertEquals(CHECKING_ACCOUNT_ID, saved.getAccountId());
    assertEquals(0L, saved.getBalance());
  }

  @Test
  @DisplayName("내 계좌가 아니면 예외를 던지고 저금통을 만들지 않는다")
  void createMoneyBox_notOwnedAccount_throws() {
    when(accountMapper.findByIdAndUserId(CHECKING_ACCOUNT_ID, USER_ID))
        .thenReturn(null);

    assertThrows(BusinessException.class,
        () -> moneyBoxService.createMoneyBox(USER_ID, request(CHECKING_ACCOUNT_ID, null)));

    verify(moneyBoxMapper, never()).insert(any());
  }

  @Test
  @DisplayName("입출금통장이 아니면 저금통을 만들 수 없다")
  void createMoneyBox_nonCheckingAccount_throws() {
    when(accountMapper.findByIdAndUserId(SAVINGS_ACCOUNT_ID, USER_ID))
        .thenReturn(account(SAVINGS_ACCOUNT_ID, "SAVINGS"));

    assertThrows(BusinessException.class,
        () -> moneyBoxService.createMoneyBox(USER_ID, request(SAVINGS_ACCOUNT_ID, null)));

    verify(moneyBoxMapper, never()).insert(any());
  }

  @Test
  @DisplayName("급여 계좌의 저금통이면 자동이체를 설정한다")
  void createMoneyBox_salaryAccount_createsAutoTransfer() {
    when(accountMapper.findByIdAndUserId(CHECKING_ACCOUNT_ID, USER_ID))
        .thenReturn(account(CHECKING_ACCOUNT_ID, "CHECKING"));
    when(transactionQueryService.getSalaryAccountId(USER_ID))
        .thenReturn(CHECKING_ACCOUNT_ID);

    moneyBoxService.createMoneyBox(USER_ID, request(CHECKING_ACCOUNT_ID, autoTransfer()));

    verify(autoTransferService).createMoneyBoxAutoTransfer(
        anyLong(), any(), anyLong(), anyString(), any());
  }

  @Test
  @DisplayName("급여 계좌가 아닌 통장에는 자동이체를 설정할 수 없다")
  void createMoneyBox_nonSalaryAccount_autoTransferRejected() {
    when(accountMapper.findByIdAndUserId(CHECKING_ACCOUNT_ID, USER_ID))
        .thenReturn(account(CHECKING_ACCOUNT_ID, "CHECKING"));
    when(transactionQueryService.getSalaryAccountId(USER_ID))
        .thenReturn(999L);

    assertThrows(BusinessException.class,
        () -> moneyBoxService.createMoneyBox(USER_ID, request(CHECKING_ACCOUNT_ID, autoTransfer())));

    verify(autoTransferService, never())
        .createMoneyBoxAutoTransfer(any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("급여 계좌를 특정할 수 없으면 자동이체를 막지 않는다")
  void createMoneyBox_salaryAccountUnknown_allowsAutoTransfer() {
    when(accountMapper.findByIdAndUserId(CHECKING_ACCOUNT_ID, USER_ID))
        .thenReturn(account(CHECKING_ACCOUNT_ID, "CHECKING"));
    when(transactionQueryService.getSalaryAccountId(USER_ID))
        .thenReturn(null);

    moneyBoxService.createMoneyBox(USER_ID, request(CHECKING_ACCOUNT_ID, autoTransfer()));

    verify(autoTransferService).createMoneyBoxAutoTransfer(
        anyLong(), any(), anyLong(), anyString(), any());
  }

  private Account account(Long accountId, String accountType) {
    Account account = Account.of(
        USER_ID, 1L, 999L, accountType, "테스트 계좌",
        new byte[]{1}, "hash", "123*****90", 100_000L, "ACTIVE",
        null, null, null, null
    );
    ReflectionTestUtils.setField(account, "accountId", accountId);
    return account;
  }

  private MoneyBoxCreateRequest request(Long accountId, AutoTransferCreateRequest autoTransfer) {
    MoneyBoxCreateRequest request = new MoneyBoxCreateRequest();
    ReflectionTestUtils.setField(request, "accountId", accountId);
    ReflectionTestUtils.setField(request, "moneyBoxType", MoneyBoxType.GOAL);
    ReflectionTestUtils.setField(request, "autoTransfer", autoTransfer);
    return request;
  }

  private AutoTransferCreateRequest autoTransfer() {
    AutoTransferCreateRequest request = new AutoTransferCreateRequest();
    ReflectionTestUtils.setField(request, "amount", 300_000L);
    ReflectionTestUtils.setField(request, "transferDay", 10);
    return request;
  }
}
