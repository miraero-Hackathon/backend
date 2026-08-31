package org.jejuro.miraero.domain.mydata.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "마이데이터 연결 완료 응답")
public class MyDataConnectResponse {

  @ApiModelProperty(value = "마이데이터 제공기관의 사용자 ID", example = "10001")
  private Long kbUserId;
}
