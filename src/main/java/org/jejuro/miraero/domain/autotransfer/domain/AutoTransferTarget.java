package org.jejuro.miraero.domain.autotransfer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자동이체 실행에 필요한 정보만 모은 조회 결과.
 * 저금통과 소속 계좌를 조인해 가져오므로 실행 시 추가 조회가 필요 없다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoTransferTarget {

  private Long autoTransferId;
  private Long moneyBoxId;
  private Long accountId;
  private Long userId;
  private Long transferAmount;
  // 계좌 잔액에서 저금통에 묶인 금액을 뺀, 실제로 쓸 수 있는 금액
  private Long availableBalance;
}
