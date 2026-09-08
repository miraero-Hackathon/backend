package org.jejuro.miraero.domain.utilitypayment.dto.response;

import java.time.YearMonth;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UtilityPaymentExecutionResponse {
    private YearMonth targetMonth;
    private int processedCount;
}
