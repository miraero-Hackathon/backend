package org.jejuro.miraero.domain.loansimulation.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "대출 시뮬레이션 즉시 실행 응답")
public class LoanDelinquencyExecutionResponse {

    @ApiModelProperty(value = "실행 기준일", example = "2026-09-06")
    private LocalDate executionDate;

    @ApiModelProperty(value = "판정 처리된 목표 건수", example = "3")
    private int processedCount;
}
