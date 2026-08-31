package org.jejuro.miraero.domain.goal.dto.request;


import lombok.AllArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.AssetType;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ApiModel(description = "목표 연결 자산 항목")
public class GoalAssetRequest {
    @ApiModelProperty(value = "자산 유형", example = "ACCOUNT")
    private AssetType assetType;
    @ApiModelProperty(value = "자산 ID", example = "1")
    private Long assetId;
}
