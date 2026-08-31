package org.jejuro.miraero.domain.mydata.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.mydata.client.MyDataApiClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataAccountResponse;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTransactionResponse;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
import org.jejuro.miraero.domain.mydata.repository.MyDataTokenRepository;
import org.jejuro.miraero.domain.transaction.domain.TransactionSyncCommand;
import org.jejuro.miraero.domain.transaction.mapper.TransactionSyncMapper;
import org.jejuro.miraero.global.crypto.AccountNumberCrypto;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MyDataSyncServiceImplTest {

  private static final Long USER_ID = 1L;
  private static final Long KB_USER_ID = 10001L;

  @Mock
  private MyDataApiClient myDataApiClient;
  @Mock
  private MyDataTokenRepository myDataTokenRepository;
  @Mock
  private ReferenceDataMapper referenceDataMapper;
  @Mock
  private AccountMapper accountMapper;
  @Mock
  private TransactionSyncMapper transactionSyncMapper;

  private MyDataSyncService myDataSyncService;

  @BeforeEach
  void setUp() {
    myDataSyncService = new MyDataSyncServiceImpl(
        myDataApiClient,
        myDataTokenRepository,
        referenceDataMapper,
        accountMapper,
        transactionSyncMapper,
        new AccountNumberCrypto()
    );
  }

  @Test
  @DisplayName("토큰이 없으면 예외를 던지고 외부 호출을 하지 않는다")
  void sync_tokenMissing() {
    when(myDataTokenRepository.findByUserId(USER_ID)).thenReturn(null);

    assertThrows(BusinessException.class, () -> myDataSyncService.sync(USER_ID, KB_USER_ID));

    verify(myDataApiClient, never()).getAccounts(any(), any());
  }

  @Test
  @DisplayName("등록되지 않은 금융기관 코드면 예외를 던진다")
  void sync_unknownInstitution() {
    when(myDataTokenRepository.findByUserId(USER_ID)).thenReturn("token-1");
    when(myDataApiClient.getAccounts(KB_USER_ID, "token-1"))
        .thenReturn(List.of(new MyDataAccountResponse()));
    when(referenceDataMapper.findFinancialInstitutionIdByCode(null)).thenReturn(null);

    assertThrows(BusinessException.class, () -> myDataSyncService.sync(USER_ID, KB_USER_ID));

    verify(transactionSyncMapper, never()).upsert(any());
  }

  @Test
  @DisplayName("정상 동기화 시 계좌를 먼저 저장한 뒤 거래를 저장한다")
  void sync_syncsAccountsBeforeTransactions() {
    when(myDataTokenRepository.findByUserId(USER_ID)).thenReturn("token-1");
    when(referenceDataMapper.findFinancialInstitutionIdByCode("004")).thenReturn(1L);
    when(myDataApiClient.getAccounts(KB_USER_ID, "token-1"))
        .thenReturn(List.of(createAccountResponse(201L, "1234567890")));
    when(accountMapper.findAccountIdByExAccountId(201L)).thenReturn(99L);
    when(referenceDataMapper.findExpenseCategoryIdByName("카페")).thenReturn(9L);
    when(myDataApiClient.getTransactions(KB_USER_ID, "token-1"))
        .thenReturn(List.of(createTransactionResponse(301L, 201L, "카페")));

    myDataSyncService.sync(USER_ID, KB_USER_ID);

    InOrder order = inOrder(myDataApiClient, accountMapper, transactionSyncMapper);
    order.verify(myDataApiClient).getAccounts(KB_USER_ID, "token-1");
    order.verify(accountMapper).upsert(any());
    order.verify(myDataApiClient).getTransactions(KB_USER_ID, "token-1");
    order.verify(transactionSyncMapper).upsert(any());
  }

  @Test
  @DisplayName("매칭되는 카테고리가 없어도 거래를 버리지 않고 null 카테고리로 저장한다")
  void sync_unmatchedCategory_stillPersistsTransaction() {
    when(myDataTokenRepository.findByUserId(USER_ID)).thenReturn("token-1");
    when(myDataApiClient.getAccounts(KB_USER_ID, "token-1")).thenReturn(List.of());
    when(referenceDataMapper.findExpenseCategoryIdByName("존재하지않는카테고리")).thenReturn(null);
    when(myDataApiClient.getTransactions(KB_USER_ID, "token-1"))
        .thenReturn(List.of(createTransactionResponse(301L, null, "존재하지않는카테고리")));

    myDataSyncService.sync(USER_ID, KB_USER_ID);

    ArgumentCaptor<TransactionSyncCommand> captor = ArgumentCaptor.forClass(TransactionSyncCommand.class);
    verify(transactionSyncMapper).upsert(captor.capture());
    assertNull(captor.getValue().getExpenseCategoryId());
    assertNull(captor.getValue().getAccountId());
  }

  private MyDataAccountResponse createAccountResponse(Long accountId, String accountNumber) {
    MyDataAccountResponse response = new MyDataAccountResponse();
    ReflectionTestUtils.setField(response, "accountId", accountId);
    ReflectionTestUtils.setField(response, "financialInstitutionCode", "004");
    ReflectionTestUtils.setField(response, "accountType", "CHECKING");
    ReflectionTestUtils.setField(response, "accountName", "KB 입출금통장");
    ReflectionTestUtils.setField(response, "accountNumber", accountNumber);
    ReflectionTestUtils.setField(response, "balance", 3400000L);
    ReflectionTestUtils.setField(response, "accountStatus", "ACTIVE");
    return response;
  }

  private MyDataTransactionResponse createTransactionResponse(
      Long transactionId, Long accountId, String categoryName
  ) {
    MyDataTransactionResponse response = new MyDataTransactionResponse();
    ReflectionTestUtils.setField(response, "transactionId", transactionId);
    ReflectionTestUtils.setField(response, "accountId", accountId);
    ReflectionTestUtils.setField(response, "transactionType", "WITHDRAWAL");
    ReflectionTestUtils.setField(response, "amount", 5500L);
    ReflectionTestUtils.setField(response, "balanceAfter", 3394500L);
    ReflectionTestUtils.setField(response, "categoryName", categoryName);
    return response;
  }
}
