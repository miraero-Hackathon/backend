package org.jejuro.miraero.domain.pacemaker.dto.request;

import javax.validation.constraints.Positive;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@ApiModel(description = "페이스메이커 개설 요청")
public class PaceMakerCreateRequest {

  @ApiModelProperty(
      value = "페이스메이커 저금통을 만들 입출금 계좌 ID. 생략하면 급여가 입금되는 통장으로 자동 지정됩니다.",
      example = "1"
  )
  private Long accountId;

  @ApiModelProperty(value = "1회 적립 상한. 생략하면 남은 여유자금 전액을 적립합니다.", example = "30000")
  @Positive
  private Long maxAmount;
}
