package org.jejuro.miraero.domain.account.dto.response;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "내 계좌 목록 응답")
public class AccountListResponse {

  @ApiModelProperty(value = "조회된 계좌 잔액 합계(원)", example = "3400000")
  private Long totalBalance;
  @ApiModelProperty(value = "계좌 목록")
  private List<AccountResponse> accounts;
}
