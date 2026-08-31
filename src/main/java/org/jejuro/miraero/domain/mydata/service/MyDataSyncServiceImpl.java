package org.jejuro.miraero.domain.mydata.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.mydata.client.MyDataApiClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataAccountResponse;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTransactionResponse;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
import org.jejuro.miraero.domain.mydata.repository.MyDataTokenRepository;
import org.jejuro.miraero.domain.transaction.domain.TransactionSyncCommand;
import org.jejuro.miraero.domain.transaction.mapper.TransactionSyncMapper;
import org.jejuro.miraero.global.crypto.AccountNumberCrypto;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyDataSyncServiceImpl implements MyDataSyncService {

  private final MyDataApiClient myDataApiClient;
  private final MyDataTokenRepository myDataTokenRepository;
  private final ReferenceDataMapper referenceDataMapper;
  private final AccountMapper accountMapper;
  private final TransactionSyncMapper transactionSyncMapper;
  private final AccountNumberCrypto accountNumberCrypto;

  @Override
  @Transactional
  public void sync(Long userId, Long kbUserId) {
    String accessToken = myDataTokenRepository.findByUserId(userId);
    if (accessToken == null) {
      throw new BusinessException(MyDataErrorCode.MYDATA_TOKEN_EXPIRED);
    }

    // 거래의 account_id는 계좌의 ex_account_id를 역조회해 채우므로 계좌가 먼저 저장돼 있어야 한다
    syncAccounts(userId, kbUserId, accessToken);
    syncTransactions(userId, kbUserId, accessToken);
  }

  private void syncAccounts(Long userId, Long kbUserId, String accessToken) {
    List<MyDataAccountResponse> accounts = myDataApiClient.getAccounts(kbUserId, accessToken);

    for (MyDataAccountResponse source : accounts) {
      Long financialInstitutionId =
          referenceDataMapper.findFinancialInstitutionIdByCode(source.getFinancialInstitutionCode());
      if (financialInstitutionId == null) {
        throw new BusinessException(MyDataErrorCode.MYDATA_INSTITUTION_NOT_FOUND);
      }

      String accountNumber = source.getAccountNumber();
      accountMapper.upsert(Account.of(
          userId,
          financialInstitutionId,
          source.getAccountId(),
          source.getAccountType(),
          source.getAccountName(),
          accountNumberCrypto.encrypt(accountNumber),
          accountNumberCrypto.hashForAccount(accountNumber),
          accountNumberCrypto.mask(accountNumber),
          source.getBalance(),
          source.getAccountStatus(),
          source.getOpenedAt(),
          source.getMaturityAt(),
          source.getInterestRate(),
          source.getMonthlyPaymentLimit()
      ));
    }
  }

  private void syncTransactions(Long userId, Long kbUserId, String accessToken) {
    List<MyDataTransactionResponse> transactions =
        myDataApiClient.getTransactions(kbUserId, accessToken);

    for (MyDataTransactionResponse source : transactions) {
      // 매칭되는 카테고리가 없어도 거래 자체는 버리지 않고 expense_category_id를 null로 저장한다
      transactionSyncMapper.upsert(new TransactionSyncCommand(
          userId,
          resolveInternalAccountId(source.getAccountId()),
          referenceDataMapper.findExpenseCategoryIdByName(source.getCategoryName()),
          source.getTransactionId(),
          source.getTransactionType(),
          source.getAmount(),
          source.getBalanceAfter(),
          source.getTransactedAt(),
          source.getMerchantName()
      ));
    }
  }

  private Long resolveInternalAccountId(Long exAccountId) {
    if (exAccountId == null) {
      return null;
    }
    return accountMapper.findAccountIdByExAccountId(exAccountId);
  }
}
