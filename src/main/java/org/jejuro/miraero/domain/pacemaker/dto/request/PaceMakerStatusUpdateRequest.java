package org.jejuro.miraero.domain.pacemaker.dto.request;

import javax.validation.constraints.NotBlank;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@ApiModel(description = "페이스메이커 상태 변경 요청")
public class PaceMakerStatusUpdateRequest {

  @ApiModelProperty(value = "변경할 상태. ACTIVE 또는 PAUSED", required = true, example = "ACTIVE")
  @NotBlank(message = "변경할 자동저축 상태는 필수입니다.")
  @Pattern(regexp = "ACTIVE|PAUSED", message = "자동저축 상태는 ACTIVE 또는 PAUSED만 가능합니다.")
  private String status;
}
