package org.jejuro.miraero.domain.transaction.dto.request;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ExpenseAnalysisSearchCondition {

    private final Integer year;
    private final Integer month;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public ExpenseAnalysisSearchCondition(Integer year, Integer month) {
        this.year = year;
        this.month = month;
    }

    public void setDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
}
