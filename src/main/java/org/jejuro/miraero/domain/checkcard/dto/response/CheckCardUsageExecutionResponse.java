package org.jejuro.miraero.domain.checkcard.dto.response;

import java.time.YearMonth;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckCardUsageExecutionResponse {
    private YearMonth targetMonth;
    private int processedCount;
}
