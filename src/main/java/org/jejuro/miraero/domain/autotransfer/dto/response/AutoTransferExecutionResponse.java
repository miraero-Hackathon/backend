package org.jejuro.miraero.domain.autotransfer.dto.response;

import java.time.LocalDate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "자동 이체 실행 결과")
public class AutoTransferExecutionResponse {

    @ApiModelProperty(value = "실행 기준일", example = "2026-08-12")
    private LocalDate executionDate;
    @ApiModelProperty(value = "실행된 자동 이체 건수", example = "3")
    private int executedCount;
}
