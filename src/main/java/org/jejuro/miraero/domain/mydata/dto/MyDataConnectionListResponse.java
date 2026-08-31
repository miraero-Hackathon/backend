package org.jejuro.miraero.domain.mydata.dto;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "마이데이터 연결 목록 응답")
public class MyDataConnectionListResponse {

  @ApiModelProperty(value = "금융기관별 연결 정보")
  private List<MyDataConnectionResponse> connections;
}
