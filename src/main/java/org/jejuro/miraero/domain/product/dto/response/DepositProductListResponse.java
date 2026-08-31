package org.jejuro.miraero.domain.product.dto.response;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "예금 상품 목록 응답")
public class DepositProductListResponse {

    @ApiModelProperty(value = "예금 상품 목록")
    private List<DepositProductResponse> products;
}
