package org.jejuro.miraero.domain.loansimulation.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoanDelinquencyExecutionResponse {
    private LocalDate executionDate;
    private int processedCount;
}