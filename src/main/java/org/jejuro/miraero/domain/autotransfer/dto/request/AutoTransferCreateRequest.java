package org.jejuro.miraero.domain.autotransfer.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Getter;

@Getter
@ApiModel(description = "자동 이체 생성 설정")
public class AutoTransferCreateRequest {

    @ApiModelProperty(value = "매월 자동 이체 금액(원)", required = true, example = "50000")
    @NotNull
    @Positive
    private Long amount;

    @ApiModelProperty(value = "매월 자동 이체일(1~31)", required = true, example = "25")
    @NotNull
    @Min(1)
    @Max(31)
    private Integer transferDay;
}
