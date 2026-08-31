package org.jejuro.miraero.domain.account.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "계좌 정보")
public class AccountResponse {

  @ApiModelProperty(value = "계좌 ID", example = "1")
  private Long accountId;
  @ApiModelProperty(value = "계좌 유형. CHECKING, SAVINGS, DEPOSIT, INSTALLMENT, ISA, CMA 중 하나", example = "SAVINGS")
  private String accountType;
  @ApiModelProperty(value = "계좌명", example = "KB국민 입출금통장")
  private String accountName;
  @ApiModelProperty(value = "금융기관명", example = "국민은행")
  private String institutionName;
  @ApiModelProperty(value = "마스킹된 계좌번호", example = "123*****90")
  private String maskedAccountNumber;
  @ApiModelProperty(value = "현재 잔액(원)", example = "3400000")
  private Long balance;
  @ApiModelProperty(value = "계좌 상태", example = "ACTIVE")
  private String accountStatus;
  @ApiModelProperty(value = "계좌 개설일", example = "2025-01-01")
  private LocalDate openedAt;
  @ApiModelProperty(value = "만기일. 입출금 계좌 등에는 null일 수 있음", example = "2027-01-01")
  private LocalDate maturityAt;
  @ApiModelProperty(value = "적용 금리(%). 해당하지 않는 계좌에는 null일 수 있음", example = "3.50")
  private BigDecimal interestRate;
  @ApiModelProperty(value = "월 납입 한도(원). 해당하지 않는 계좌에는 null일 수 있음", example = "500000")
  private Long monthlyPaymentLimit;
}
