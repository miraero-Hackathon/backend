package org.jejuro.miraero.domain.moneybox.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;

@Getter
@Builder
@ApiModel(description = "머니박스 생성 응답")
public class MoneyBoxCreateResponse {

    @ApiModelProperty(value = "머니박스 ID", example = "1")
    private Long moneyBoxId;

    @ApiModelProperty(value = "연결된 입출금 계좌 ID", example = "1")
    private Long accountId;

    @ApiModelProperty(value = "머니박스 유형")
    private MoneyBoxType moneyBoxType;
}
