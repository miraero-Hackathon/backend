package org.jejuro.miraero.domain.account.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Account {

  private Long accountId;
  private Long userId;
  private Long financialInstitutionId;
  private Long exAccountId; // mock-server(외부)가 부여한 계좌 ID — upsert 시 동일 계좌 식별 기준
  private String accountType;
  private String accountName;
  private byte[] accountNumber; // 암호화된 원본 계좌번호 (평문 저장 금지)
  private String accountNumberHash; // 검색/중복확인용 해시 (암호화된 값은 검색 불가하므로 별도 보관)
  private String maskedAccountNumber; // 화면 표시용 마스킹 값
  private Long balance;
  private String accountStatus;
  private LocalDate openedAt;
  private LocalDate maturityAt;
  private BigDecimal interestRate;
  private Long monthlyPaymentLimit;

  public static Account of(
      Long userId,
      Long financialInstitutionId,
      Long exAccountId,
      String accountType,
      String accountName,
      byte[] accountNumber,
      String accountNumberHash,
      String maskedAccountNumber,
      Long balance,
      String accountStatus,
      LocalDate openedAt,
      LocalDate maturityAt,
      BigDecimal interestRate,
      Long monthlyPaymentLimit
  ) {
    return new Account(
        null, // accountId는 upsert 시 DB가 채움(신규면 AUTO_INCREMENT, 기존이면 무시됨)
        userId,
        financialInstitutionId,
        exAccountId,
        accountType,
        accountName,
        accountNumber,
        accountNumberHash,
        maskedAccountNumber,
        balance,
        accountStatus,
        openedAt,
        maturityAt,
        interestRate,
        monthlyPaymentLimit
    );
  }
}
