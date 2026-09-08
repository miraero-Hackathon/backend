package org.jejuro.miraero.domain.utilitypayment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilityPaymentState {

    private Long utilityPaymentStateId;
    private Long userId;
    private UtilityType utilityType;

    private int consecutivePaidMonths;
    private int consecutiveMissedMonths;

    // 납부 (납부할때마다 연속 납부 count가 증가 및 연체 count 초기화)
    public void markPaid() {
        this.consecutivePaidMonths += 1;
        this.consecutiveMissedMonths = 0;
    }

    // 미납 (미납할때 마다 연체 count 증가 및 연속 납부 count 초기화)
    public void markMissed() {
        this.consecutivePaidMonths = 0;
        this.consecutiveMissedMonths += 1;
    }
}
