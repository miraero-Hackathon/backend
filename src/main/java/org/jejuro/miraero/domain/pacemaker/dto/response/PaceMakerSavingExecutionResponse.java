package org.jejuro.miraero.domain.pacemaker.dto.response;

import java.time.LocalDate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "페이스메이커 적립 실행 결과")
public class PaceMakerSavingExecutionResponse {

  @ApiModelProperty(value = "정산한 영업일", example = "2026-08-11")
  private LocalDate businessDate;

  @ApiModelProperty(value = "실제로 적립된 건수", example = "1")
  private int savedCount;
}
