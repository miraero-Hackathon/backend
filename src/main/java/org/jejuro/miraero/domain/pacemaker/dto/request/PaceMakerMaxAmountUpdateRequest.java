package org.jejuro.miraero.domain.pacemaker.dto.request;

import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Positive;
import lombok.Getter;

@Getter
@ApiModel(description = "페이스메이커 최대 저축 금액 변경 요청")
public class PaceMakerMaxAmountUpdateRequest {

  @ApiModelProperty(value = "자동 저축 1회 최대 금액(원)", required = true, example = "30000")
  @NotNull(message = "자동저축 월 상한액은 필수입니다.")
  @Positive(message = "자동저축 월 상한액은 0보다 커야 합니다.")
  private Long maxAmount;

}
