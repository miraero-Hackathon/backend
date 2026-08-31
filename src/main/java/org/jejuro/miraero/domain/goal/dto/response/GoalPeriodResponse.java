package org.jejuro.miraero.domain.goal.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.YearMonth;

@Getter
@Builder
@AllArgsConstructor
public class GoalPeriodResponse {
    private Integer goalMonths;

    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth startDate;

    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth endDate;
    private Integer remainMonths;
}
