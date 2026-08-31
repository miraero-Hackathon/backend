package org.jejuro.miraero.domain.moneybox.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.dto.request.AutoTransferCreateRequest;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;

@Getter
@NoArgsConstructor
@ApiModel(description = "머니박스 생성 요청")
public class MoneyBoxCreateRequest {

    @ApiModelProperty(
        value = "머니박스를 연결할 입출금 계좌 ID",
        required = true,
        example = "1"
    )
    @NotNull
    private Long accountId;

    @ApiModelProperty(value = "머니박스 유형", required = true)
    @NotNull
    private MoneyBoxType moneyBoxType;

    @ApiModelProperty(value = "자동 이체 설정. 자동 이체를 사용하지 않으면 null")
    @Valid
    private AutoTransferCreateRequest autoTransfer;
}