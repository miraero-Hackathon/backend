package org.jejuro.miraero.domain.moneybox.domain;


import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBox {

  private Long moneyBoxId;
  private Long userId;
  // 저금통이 속한 입출금통장. 저금통 잔액은 이 계좌 안에 있는 돈이다.
  private Long accountId;
  private Long balance;
  private MoneyBoxType moneyBoxType;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
