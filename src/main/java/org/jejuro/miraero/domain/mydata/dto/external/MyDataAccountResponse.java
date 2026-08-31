package org.jejuro.miraero.domain.mydata.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyDataAccountResponse {

  private Long accountId;
  private Long kbUserId;
  private String financialInstitutionCode;
  private String accountType;
  private String accountName;
  private String accountNumber;
  private Long balance;
  private String accountStatus;
  private LocalDate openedAt;
  private LocalDate maturityAt;
  private BigDecimal interestRate;
  private Long monthlyPaymentLimit;
}
